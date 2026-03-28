package com.example.transactiondispute.cashmanagementapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.transactiondispute.R;
import com.example.transactiondispute.SupabaseConfig;
import com.example.transactiondispute.api.SupabaseService;
import com.example.transactiondispute.models.DispenseData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdminDispenseListActivity extends AppCompatActivity {

    private RecyclerView rvAtmList;
    private ProgressBar progressBar;
    private AtmAdapter adapter;
    private SupabaseService supabaseService;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dispense_list);

        loadSession();
        initRetrofit();
        initializeViews();
        loadAtms();
    }

    private void loadSession() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
    }

    private void initRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request().newBuilder()
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

    private void initializeViews() {
        rvAtmList = findViewById(R.id.rvAtmList);
        progressBar = findViewById(R.id.progressBar);
        rvAtmList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AtmAdapter(new ArrayList<>());
        rvAtmList.setAdapter(adapter);
    }

    private void loadAtms() {
        progressBar.setVisibility(View.VISIBLE);
        String authHeader = "Bearer " + accessToken;

        supabaseService.getDispenseData(authHeader, "franchisee_id", null).enqueue(new Callback<List<DispenseData>>() {
            @Override
            public void onResponse(Call<List<DispenseData>> call, Response<List<DispenseData>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Set<String> uniqueAtms = new HashSet<>();
                    for (DispenseData data : response.body()) {
                        if (data.franchiseeId != null) {
                            uniqueAtms.add(data.franchiseeId);
                        }
                    }
                    adapter.updateData(new ArrayList<>(uniqueAtms));
                } else {
                    Toast.makeText(AdminDispenseListActivity.this, "Failed to load ATMs", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DispenseData>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminDispenseListActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class AtmAdapter extends RecyclerView.Adapter<AtmAdapter.ViewHolder> {
        private List<String> atmList;

        AtmAdapter(List<String> atmList) {
            this.atmList = atmList;
        }

        void updateData(List<String> newList) {
            this.atmList = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String atmId = atmList.get(position);
            holder.textView.setText("ATM ID: " + atmId);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDispenseListActivity.this, DispenseAnalyticsActivity.class);
                intent.putExtra("EXTRA_FRANCHISEE_ID", atmId);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return atmList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(View v) {
                super(v);
                textView = v.findViewById(android.R.id.text1);
            }
        }
    }
}
