package com.example.transactiondispute.cashmanagementapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class CashmanagementActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private CashDataAdapter adapter;
    private List<CashData> dataList;
    private SupabaseService supabaseService;
    private String accessToken, userRole;
    private ProgressBar progressBar;
    private TextView tvSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadSession();
        initRetrofit();
        setContentView(R.layout.activity_cashmanagement);
        
        initializeViews();
        setupRecyclerView();
        loadDataFromCloud();
    }

    private void loadSession() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        userRole = prefs.getString("user_role", "");
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

    private void loadDataFromCloud() {
        progressBar.setVisibility(View.VISIBLE);
        String authHeader = "Bearer " + accessToken;
        
        // If Admin, they see all data due to RLS policies we set in SQL
        supabaseService.getCashData(authHeader, "*").enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    processCloudData(response.body());
                } else {
                    Toast.makeText(CashmanagementActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CashmanagementActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processCloudData(List<Map<String, Object>> cloudData) {
        dataList = new ArrayList<>();
        int totalLoad = 0;
        
        for (Map<String, Object> map : cloudData) {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        adapter.updateData(dataList);
        tvSummary.setText("Total Load: ₹" + totalLoad + " | Records: " + dataList.size());
        if (userRole.equals("admin")) {
            tvSummary.append("\n(Logged in as Admin)");
        }
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvSummary = findViewById(R.id.tvDataCount); // Reusing existing ID
        
        findViewById(R.id.btngotoDataEntry).setOnClickListener(v -> {
            startActivityForResult(new Intent(this, DataEntryActivity.class), 1001);
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CashDataAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) loadDataFromCloud();
    }
}
