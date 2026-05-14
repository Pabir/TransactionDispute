package com.example.transactiondispute.cashmanagementapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.transactiondispute.LoginActivity;
import com.example.transactiondispute.R;
import com.example.transactiondispute.SupabaseConfig;
import com.example.transactiondispute.api.SupabaseService;
import com.example.transactiondispute.models.AuthModels;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdminCashDataActivity extends AppCompatActivity {

    private RecyclerView rvAtmList;
    private AtmAdapter adapter;
    private SupabaseService supabaseService;
    private String accessToken;
    private List<AuthModels.Profile> allProfiles = new ArrayList<>();
    private Set<String> franchiseesWithEntry = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_cash_data);

        loadSession();
        initRetrofit();
        
        rvAtmList = findViewById(R.id.rvAtmList);
        rvAtmList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AtmAdapter(new ArrayList<>());
        rvAtmList.setAdapter(adapter);

        loadProfilesAndData();
    }

    private void loadSession() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        if (accessToken.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void initRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("apikey", SupabaseConfig.ANON_KEY)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SupabaseConfig.URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        supabaseService = retrofit.create(SupabaseService.class);
    }

    private void loadProfilesAndData() {
        String authHeader = "Bearer " + accessToken;
        // 1. Get all profiles to know all franchisees
        supabaseService.getProfiles(authHeader, "*").enqueue(new Callback<List<AuthModels.Profile>>() {
            @Override
            public void onResponse(Call<List<AuthModels.Profile>> call, Response<List<AuthModels.Profile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allProfiles = response.body();
                    loadCashData();
                }
            }

            @Override
            public void onFailure(Call<List<AuthModels.Profile>> call, Throwable t) {
                Toast.makeText(AdminCashDataActivity.this, "Error loading profiles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCashData() {
        String authHeader = "Bearer " + accessToken;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        supabaseService.getCashData(authHeader, "*").enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processData(response.body(), today);
                } else {
                    Toast.makeText(AdminCashDataActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(AdminCashDataActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processData(List<Map<String, Object>> rawData, String today) {
        franchiseesWithEntry.clear();
        Map<String, List<CashData>> groupedMap = new HashMap<>();
        
        for (Map<String, Object> map : rawData) {
            String fId = (String) map.get("franchisee_id");
            String entryDate = (String) map.get("entry_date");
            
            if (today.equals(entryDate)) {
                franchiseesWithEntry.add(fId);
            }

            if (fId == null) fId = "Unknown";
            try {
                CashData data = new CashData(
                    entryDate,
                    ((Double) map.get("indent_amount")).intValue(),
                    ((Double) map.get("notes_500")).intValue(),
                    ((Double) map.get("notes_200")).intValue(),
                    ((Double) map.get("notes_100")).intValue(),
                    (String) map.get("eod_received"),
                    ((Double) map.get("eod_500")).intValue(),
                    ((Double) map.get("eod_200")).intValue(),
                    ((Double) map.get("eod_100")).intValue(),
                    (String) map.get("loading_time")
                );
                if (!groupedMap.containsKey(fId)) groupedMap.put(fId, new ArrayList<>());
                groupedMap.get(fId).add(data);
            } catch (Exception e) {}
        }
        
        List<AtmGroup> groups = new ArrayList<>();
        // Add everyone from profiles
        for (AuthModels.Profile profile : allProfiles) {
            if ("admin".equalsIgnoreCase(profile.role)) continue;
            String fId = profile.franchiseeId;
            List<CashData> records = groupedMap.getOrDefault(fId, new ArrayList<>());
            groups.add(new AtmGroup(fId, records, profile.fullName, franchiseesWithEntry.contains(fId)));
        }
        adapter.updateData(groups);
    }

    private static class AtmGroup {
        String atmId;
        List<CashData> records;
        String fullName;
        boolean hasEntryToday;
        AtmGroup(String id, List<CashData> r, String name, boolean hasEntry) { 
            this.atmId = id; this.records = r; this.fullName = name; this.hasEntryToday = hasEntry;
        }
    }

    private class AtmAdapter extends RecyclerView.Adapter<AtmAdapter.ViewHolder> {
        private List<AtmGroup> groups;
        AtmAdapter(List<AtmGroup> groups) { this.groups = groups; }
        
        void updateData(List<AtmGroup> newGroups) {
            this.groups = newGroups;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_atm, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AtmGroup group = groups.get(position);
            holder.tvAtmId.setText("ATM ID: " + group.atmId + " (" + group.fullName + ")");
            holder.tvStatus.setText(group.hasEntryToday ? "Status: Entry Completed" : "Status: Pending Entry");
            holder.tvStatus.setTextColor(group.hasEntryToday ? 0xFF4CAF50 : 0xFFF44336);

            // Show WhatsApp button if pending and after 7 PM (or always for testing/admin convenience)
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            
            if (!group.hasEntryToday && hour >= 19) {
                holder.btnWhatsApp.setVisibility(View.VISIBLE);
                holder.btnWhatsApp.setOnClickListener(v -> sendWhatsApp(group.atmId, group.fullName));
            } else {
                holder.btnWhatsApp.setVisibility(View.GONE);
            }
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(AdminCashDataActivity.this, AdminSingleAtmDataActivity.class);
                intent.putExtra("FILTER_ATM_ID", group.atmId);
                startActivity(intent);
            });
        }

        private void sendWhatsApp(String atmId, String name) {
            String message = "Reminder: Please complete the daily cash loading entry for ATM " + atmId + " (" + name + ").";
            try {
                // We don't have mobile number in profile, so open WhatsApp to select contact
                // Or if you want to target a specific number, you need it in the DB
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String url = "https://api.whatsapp.com/send?text=" + Uri.encode(message);
                intent.setPackage("com.whatsapp");
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(AdminCashDataActivity.this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public int getItemCount() { return groups.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAtmId, tvStatus;
            Button btnWhatsApp;
            ViewHolder(View v) {
                super(v);
                tvAtmId = v.findViewById(R.id.tvItemAtmId);
                tvStatus = v.findViewById(R.id.tvItemStatus);
                btnWhatsApp = v.findViewById(R.id.btnWhatsApp);
            }
        }
    }
}
