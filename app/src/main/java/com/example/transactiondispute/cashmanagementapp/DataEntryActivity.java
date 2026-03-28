package com.example.transactiondispute.cashmanagementapp;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.transactiondispute.LoginActivity;
import com.example.transactiondispute.R;
import com.example.transactiondispute.SupabaseConfig;
import com.example.transactiondispute.api.SupabaseService;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DataEntryActivity extends AppCompatActivity {
    
    private Button btnEntryDate, btnSave, btnCancel;
    private TextView tvSelectedEntryDate, tvLoadAmount, tvEodAmount, tvDueEodAmount;
    private EditText etIndentAmount, et500Notes, et200Notes, et100Notes;
    private EditText etEodReceived, etEod500, etEod200, etEod100, etLoadingTime;
    private ProgressBar progressBar;
    
    private String accessToken, userId, franchiseeId;
    private SupabaseService supabaseService;
    private final Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadSession();
        initRetrofit();
        setContentView(R.layout.activity_data_entry);
        
        initializeViews();
        setupEventListeners();
        setupTextWatchers();
    }

    private void loadSession() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        userId = prefs.getString("user_id", "");
        franchiseeId = prefs.getString("franchisee_id", "");
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

    private void saveData() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Select date", Toast.LENGTH_SHORT).show();
            return;
        }

        int n500 = getIntValue(et500Notes);
        int n200 = getIntValue(et200Notes);
        int n100 = getIntValue(et100Notes);
        int loadAmount = (n500 * 500) + (n200 * 200) + (n100 * 100);

        int e500 = getIntValue(etEod500);
        int e200 = getIntValue(etEod200);
        int e100 = getIntValue(etEod100);
        int eodAmount = (e500 * 500) + (e200 * 200) + (e100 * 100);

        Map<String, Object> data = new HashMap<>();
        data.put("user_id", userId);
        data.put("franchisee_id", franchiseeId);
        data.put("entry_date", selectedDate);
        data.put("indent_amount", getIntValue(etIndentAmount));
        data.put("notes_500", n500);
        data.put("notes_200", n200);
        data.put("notes_100", n100);
        data.put("load_amount", loadAmount);
        data.put("eod_received", etEodReceived.getText().toString());
        data.put("eod_500", e500);
        data.put("eod_200", e200);
        data.put("eod_100", e100);
        data.put("eod_amount", eodAmount);
        data.put("loading_time", etLoadingTime.getText().toString());

        btnSave.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        supabaseService.insertCashData("Bearer " + accessToken, data).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnSave.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(DataEntryActivity.this, "Synced to Cloud!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(DataEntryActivity.this, "Sync failed: " + error, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(DataEntryActivity.this, "Cloud sync failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSave.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DataEntryActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        btnEntryDate = findViewById(R.id.btnEntryDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        tvSelectedEntryDate = findViewById(R.id.tvSelectedEntryDate);
        tvLoadAmount = findViewById(R.id.tvLoadAmount);
        tvEodAmount = findViewById(R.id.tvEodAmount);
        tvDueEodAmount = findViewById(R.id.tvDueEodAmount);
        etIndentAmount = findViewById(R.id.etIndentAmount);
        et500Notes = findViewById(R.id.et500Notes);
        et200Notes = findViewById(R.id.et200Notes);
        et100Notes = findViewById(R.id.et100Notes);
        etEodReceived = findViewById(R.id.etEodReceived);
        etEod500 = findViewById(R.id.etEod500);
        etEod200 = findViewById(R.id.etEod200);
        etEod100 = findViewById(R.id.etEod100);
        etLoadingTime = findViewById(R.id.etLoadingTime);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupEventListeners() {
        btnEntryDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveData());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(year, month, day);
            selectedDate = dateFormat.format(calendar.getTime());
            tvSelectedEntryDate.setText("Date: " + selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupTextWatchers() {
        TextWatcher tw = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { calculateAmounts(); }
        };
        et500Notes.addTextChangedListener(tw);
        et200Notes.addTextChangedListener(tw);
        et100Notes.addTextChangedListener(tw);
        etEodReceived.addTextChangedListener(tw);
        etEod500.addTextChangedListener(tw);
        etEod200.addTextChangedListener(tw);
        etEod100.addTextChangedListener(tw);
    }

    private void calculateAmounts() {
        int loadTotal = (getIntValue(et500Notes) * 500) + (getIntValue(et200Notes) * 200) + (getIntValue(et100Notes) * 100);
        tvLoadAmount.setText("Load: ₹" + loadTotal);

        int eodReceived = getIntValue(etEodReceived);
        int eodTotal = (getIntValue(etEod500) * 500) + (getIntValue(etEod200) * 200) + (getIntValue(etEod100) * 100);
        tvEodAmount.setText("EOD Amount: ₹" + eodTotal);
        
        int dueAmount = eodReceived - eodTotal;
        tvDueEodAmount.setText("Due EOD Amount: ₹" + dueAmount);
    }

    private int getIntValue(EditText et) {
        if (et == null) return 0;
        try { return Integer.parseInt(et.getText().toString()); } catch (Exception e) { return 0; }
    }
}
