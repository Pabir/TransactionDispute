package com.example.transactiondispute.cashmanagementapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.transactiondispute.LoginActivity;
import com.example.transactiondispute.R;
import com.example.transactiondispute.SupabaseConfig;
import com.example.transactiondispute.api.SupabaseService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
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
    private TextView tvAtmId, tvStats, tvTotalCarryForward;
    private ChipGroup chipGroupFilters;
    private ImageButton btnSort;
    
    private String accessToken, atmId;
    private List<CashData> fullDataList = new ArrayList<>();
    private boolean isAscending = false;

    // Custom date range fields
    private Long startDate = null;
    private Long endDate = null;

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
        setupListeners();
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
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
        btnSort = findViewById(R.id.btnSort);
        
        tvAtmId.setText("ATM ID: " + atmId);
        
        // Update sort icon initially
        updateSortIcon();
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CashDataAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipCustom)) {
                // If dates are already selected, apply the filter.
                // Initial picker opening is now handled exclusively by the onClick listener.
                if (startDate != null && endDate != null) {
                    applyFilters();
                }
            } else {
                applyFilters();
            }
        });

        // Open picker via onClick to avoid double-triggering when selecting the chip
        findViewById(R.id.chipCustom).setOnClickListener(v -> {
            showDateRangePicker();
        });

        btnSort.setOnClickListener(v -> {
            isAscending = !isAscending;
            updateSortIcon();
            applyFilters();
        });
    }

    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Select Custom Range")
                        .build();

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            startDate = selection.first;
            endDate = selection.second;
            
            // Update Chip text with selected range
            SimpleDateFormat displaySdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
            String rangeStr = displaySdf.format(new Date(startDate)) + " - " + displaySdf.format(new Date(endDate));
            Chip customChip = findViewById(R.id.chipCustom);
            customChip.setText(rangeStr);
            
            applyFilters();
        });
        
        dateRangePicker.addOnCancelListener(dialog -> {
            // Revert to "All" if no range was ever set
            if (startDate == null) {
                chipGroupFilters.check(R.id.chipAll);
            }
        });

        dateRangePicker.show(getSupportFragmentManager(), "DATE_RANGE_PICKER");
    }

    private void updateSortIcon() {
        if (isAscending) {
            btnSort.setImageResource(R.drawable.ic_sort_ascending);
        } else {
            btnSort.setImageResource(R.drawable.ic_sort_descending);
        }
    }

    private void loadAtmData() {
        progressBar.setVisibility(View.VISIBLE);
        String authHeader = "Bearer " + accessToken;
        
        String query = "eq." + atmId;
        supabaseService.getFilteredCashData(authHeader, "*", query).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    parseRawData(response.body());
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

    private void parseRawData(List<Map<String, Object>> rawData) {
        fullDataList.clear();
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
                fullDataList.add(data);
            } catch (Exception e) { e.printStackTrace(); }
        }
        applyFilters();
    }

    private void applyFilters() {
        List<CashData> filteredList = new ArrayList<>();
        int checkedId = chipGroupFilters.getCheckedChipId();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = sdf.format(new Date());
        
        for (CashData data : fullDataList) {
            boolean matches = false;
            if (checkedId == R.id.chipAll) {
                matches = true;
            } else if (checkedId == R.id.chipToday) {
                matches = todayStr.equals(data.getDate());
            } else if (checkedId == R.id.chip7Days) {
                matches = isWithinLastDays(data.getDate(), 7);
            } else if (checkedId == R.id.chipMonth) {
                matches = isWithinCurrentMonth(data.getDate());
            } else if (checkedId == R.id.chipCustom) {
                if (startDate != null && endDate != null) {
                    matches = isWithinCustomRange(data.getDate(), startDate, endDate);
                } else {
                    matches = true;
                }
            }
            
            if (matches) filteredList.add(data);
        }

        // Sort the filtered list
        Collections.sort(filteredList, (o1, o2) -> {
            if (isAscending) {
                return o1.getDate().compareTo(o2.getDate());
            } else {
                return o2.getDate().compareTo(o1.getDate());
            }
        });

        updateUI(filteredList);
    }

    private boolean isWithinLastDays(String dateStr, int days) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date recordDate = sdf.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -days);
            // Normalize to start of day
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            
            return recordDate != null && (recordDate.after(cal.getTime()) || recordDate.equals(cal.getTime()));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isWithinCurrentMonth(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date recordDate = sdf.parse(dateStr);
            if (recordDate == null) return false;
            Calendar recordCal = Calendar.getInstance();
            recordCal.setTime(recordDate);
            return recordCal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                   recordCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isWithinCustomRange(String dateStr, long start, long end) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            // MaterialDatePicker uses UTC timestamps.
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date recordDate = sdf.parse(dateStr);
            if (recordDate == null) return false;
            long time = recordDate.getTime();
            return time >= start && time <= end;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateUI(List<CashData> list) {
        long totalLoad = 0;
        long totalCarryForward = 0;
        for (CashData d : list) {
            totalLoad += d.getSumLoadAmount();
            totalCarryForward += d.getCarryForwardAmount();
        }
        
        adapter.updateData(list);
        tvStats.setText("Total Records: " + list.size() + " | Total Load: ₹" + totalLoad);
        tvTotalCarryForward.setText("Total Carry Forward: ₹" + totalCarryForward);
    }
}
