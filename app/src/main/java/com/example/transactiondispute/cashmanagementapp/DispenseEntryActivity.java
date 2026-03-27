package com.example.transactiondispute.cashmanagementapp;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.transactiondispute.R;
import com.example.transactiondispute.SupabaseConfig;
import com.example.transactiondispute.api.SupabaseService;
import com.example.transactiondispute.models.DispenseData;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import java.util.Locale;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DispenseEntryActivity extends AppCompatActivity {

    private TextInputEditText etDate, etIndent, etDispense, etTransactions;
    private Button btnSave;
    private ProgressBar progressBar;
    private SupabaseService supabaseService;
    private String accessToken, userId, franchiseeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispense_entry);

        loadSession();
        initRetrofit();
        initializeViews();
    }

    private void loadSession() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        userId = prefs.getString("user_id", "");
        franchiseeId = prefs.getString("franchisee_id", "");
        if (accessToken.isEmpty()) {
            finish();
        }
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
        etDate = findViewById(R.id.etEntryDate);
        etIndent = findViewById(R.id.etIndentAmount);
        etDispense = findViewById(R.id.etDispenseAmount);
        etTransactions = findViewById(R.id.etNumTransactions);
        btnSave = findViewById(R.id.btnSaveDispense);
        progressBar = findViewById(R.id.progressBar);

        etDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveData());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);
            etDate.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveData() {
        String date = etDate.getText().toString();
        String indentStr = etIndent.getText().toString();
        String dispenseStr = etDispense.getText().toString();
        String transStr = etTransactions.getText().toString();

        if (date.isEmpty() || indentStr.isEmpty() || dispenseStr.isEmpty() || transStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        DispenseData data = new DispenseData(
                userId,
                franchiseeId,
                date,
                Integer.parseInt(indentStr),
                Integer.parseInt(dispenseStr),
                Integer.parseInt(transStr)
        );

        String authHeader = "Bearer " + accessToken;
        supabaseService.upsertDispenseData(authHeader, data).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(DispenseEntryActivity.this, "Data Saved Successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(DispenseEntryActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(DispenseEntryActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
