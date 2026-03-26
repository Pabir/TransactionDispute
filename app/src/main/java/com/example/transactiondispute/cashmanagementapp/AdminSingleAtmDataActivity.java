package com.example.transactiondispute.cashmanagementapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.transactiondispute.LoginActivity;
import com.example.transactiondispute.R;
import com.example.transactiondispute.SupabaseConfig;
import com.example.transactiondispute.api.SupabaseService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdminSingleAtmDataActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CashDataAdapter adapter;
    private SupabaseService supabaseService;
    private ProgressBar progressBar;
    private TextView tvTitle, tvAtmId, tvStats, tvTotalCarryForward;
    private String accessToken, atmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_single_atm_data);

        atmId = getIntent().getStringExtra("FILTER_ATM_ID");
        if (atmId == null) {
            Toast.makeText(this, "No ATM ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadSession();
        initRetrofit();
        initializeViews();
        loadAtmData();
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

    private void initializeViews() {
        tvAtmId = findViewById(R.id.tvAtmId);
        tvStats = findViewById(R.id.tvStats);
        tvTotalCarryForward = findViewById(R.id.tvTotalCarryForward);
        progressBar = findViewById(R.id.pbSingleAtm);
        recyclerView = findViewById(R.id.rvSingleAtm);
        
        tvAtmId.setText("ATM ID: " + atmId);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CashDataAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void loadAtmData() {
        progressBar.setVisibility(View.VISIBLE);
        String authHeader = "Bearer " + accessToken;
        
        // Use filtering for specific ATM
        String query = "eq." + atmId;
        supabaseService.getFilteredCashData(authHeader, "*", query).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    processData(response.body());
                } else {
                    Toast.makeText(AdminSingleAtmDataActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminSingleAtmDataActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processData(List<Map<String, Object>> rawData) {
        List<CashData> dataList = new ArrayList<>();
        long totalLoad = 0;
        long totalCarryForward = 0;
        
        for (Map<String, Object> map : rawData) {
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
                dataList.add(data);
                totalLoad += data.getSumLoadAmount();
                totalCarryForward += data.getCarryForwardAmount();
            } catch (Exception e) { e.printStackTrace(); }
        }
        
        adapter.updateData(dataList);
        tvStats.setText("Total Records: " + dataList.size() + " | Total Load: ₹" + totalLoad);
        tvTotalCarryForward.setText("Total Carry Forward: ₹" + totalCarryForward);
    }
}
