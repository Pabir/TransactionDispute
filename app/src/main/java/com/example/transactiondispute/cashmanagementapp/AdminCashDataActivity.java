package com.example.transactiondispute.cashmanagementapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        loadAdminData();
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

    private void loadAdminData() {
        String authHeader = "Bearer " + accessToken;
        supabaseService.getCashData(authHeader, "*").enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    groupDataByAtm(response.body());
                } else {
                    Toast.makeText(AdminCashDataActivity.this, "Failed to load admin data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(AdminCashDataActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void groupDataByAtm(List<Map<String, Object>> rawData) {
        Map<String, List<CashData>> groupedMap = new HashMap<>();
        for (Map<String, Object> map : rawData) {
            String atmId = (String) map.get("franchisee_id");
            if (atmId == null) atmId = "Unknown";
            
            try {
                CashData data = new CashData(
                    (String) map.get("entry_date"),
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
                
                if (!groupedMap.containsKey(atmId)) {
                    groupedMap.put(atmId, new ArrayList<>());
                }
                groupedMap.get(atmId).add(data);
            } catch (Exception e) { e.printStackTrace(); }
        }
        
        List<AtmGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<CashData>> entry : groupedMap.entrySet()) {
            groups.add(new AtmGroup(entry.getKey(), entry.getValue()));
        }
        adapter.updateData(groups);
    }

    // Inner Classes for Adapter
    private static class AtmGroup {
        String atmId;
        List<CashData> records;
        AtmGroup(String id, List<CashData> r) { this.atmId = id; this.records = r; }
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
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AtmGroup group = groups.get(position);
            holder.text1.setText("ATM ID: " + group.atmId);
            holder.text2.setText("Total Records: " + group.records.size());
            
            holder.itemView.setOnClickListener(v -> {
                // Open the new dedicated activity for single ATM data
                Intent intent = new Intent(AdminCashDataActivity.this, AdminSingleAtmDataActivity.class);
                intent.putExtra("FILTER_ATM_ID", group.atmId);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return groups.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ViewHolder(View v) {
                super(v);
                text1 = v.findViewById(android.R.id.text1);
                text2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}
