package com.example.transactiondispute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

public class AdminAllTransactionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private ProgressBar progressBar;
    private SupabaseService supabaseService;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_all_transactions);

        loadSession();
        initRetrofit();
        initializeViews();
        loadAllTransactions();
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
        progressBar = findViewById(R.id.pbAllTransactions);
        recyclerView = findViewById(R.id.rvAllTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void loadAllTransactions() {
        progressBar.setVisibility(View.VISIBLE);
        String authHeader = "Bearer " + accessToken;

        supabaseService.getAllTransactions(authHeader, "*", "created_at.desc").enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body());
                } else {
                    Toast.makeText(AdminAllTransactionsActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminAllTransactionsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
        private List<Map<String, Object>> transactions;

        public TransactionAdapter(List<Map<String, Object>> transactions) {
            this.transactions = transactions;
        }

        public void updateData(List<Map<String, Object>> newTransactions) {
            this.transactions = newTransactions;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> t = transactions.get(position);
            
            String type = (String) t.get("transaction_type");
            holder.tvType.setText(type != null ? type : "N/A");
            if (type != null && type.startsWith("CARD")) {
                holder.tvType.setBackgroundColor(Color.parseColor("#2196F3")); // Blue for Card
            } else {
                holder.tvType.setBackgroundColor(Color.parseColor("#4CAF50")); // Green for UPI
            }

            holder.tvAtmId.setText((String) t.get("atm_id"));
            holder.tvDate.setText((String) t.get("trans_date"));
            holder.tvCustomer.setText("Customer: " + t.get("customer_name"));
            holder.tvAmount.setText("Amount: ₹" + t.get("dispute_amount"));
            holder.tvUtr.setText("UTR: " + t.get("utr"));
            holder.tvFranchisee.setText("By: " + t.get("franchisee_id"));
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvType, tvAtmId, tvDate, tvCustomer, tvAmount, tvUtr, tvFranchisee;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvType = itemView.findViewById(R.id.tvItemType);
                tvAtmId = itemView.findViewById(R.id.tvItemAtmId);
                tvDate = itemView.findViewById(R.id.tvItemDate);
                tvCustomer = itemView.findViewById(R.id.tvItemCustomer);
                tvAmount = itemView.findViewById(R.id.tvItemAmount);
                tvUtr = itemView.findViewById(R.id.tvItemUtr);
                tvFranchisee = itemView.findViewById(R.id.tvItemFranchisee);
            }
        }
    }
}
