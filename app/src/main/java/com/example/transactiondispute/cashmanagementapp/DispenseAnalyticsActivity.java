package com.example.transactiondispute.cashmanagementapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.transactiondispute.R;
import com.example.transactiondispute.SupabaseConfig;
import com.example.transactiondispute.api.SupabaseService;
import com.example.transactiondispute.models.DispenseData;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.tabs.TabLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DispenseAnalyticsActivity extends AppCompatActivity {

    private TextView tvDayStats, tvDayPercent;
    private TextView tvWeekStats, tvWeekPercent;
    private TextView tvMonthStats, tvMonthPercent;
    private Spinner spinnerDay, spinnerWeek, spinnerMonth;
    private ProgressBar progressBar;
    private Button btnRefresh;
    private BarChart barChart;
    private TabLayout tabLayout;
    private SupabaseService supabaseService;
    private String accessToken, franchiseeId;
    private List<DispenseData> allData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispense_analytics);

        loadSession();
        initRetrofit();
        initializeViews();
        loadAnalytics();
    }

    private void loadSession() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        if (getIntent().hasExtra("EXTRA_FRANCHISEE_ID")) {
            franchiseeId = getIntent().getStringExtra("EXTRA_FRANCHISEE_ID");
        } else {
            franchiseeId = prefs.getString("franchisee_id", "");
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
        tvDayStats = findViewById(R.id.tvDayStats);
        tvDayPercent = findViewById(R.id.tvDayPercent);
        tvWeekStats = findViewById(R.id.tvWeekStats);
        tvWeekPercent = findViewById(R.id.tvWeekPercent);
        tvMonthStats = findViewById(R.id.tvMonthStats);
        tvMonthPercent = findViewById(R.id.tvMonthPercent);
        
        spinnerDay = findViewById(R.id.spinnerDay);
        spinnerWeek = findViewById(R.id.spinnerWeek);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        
        progressBar = findViewById(R.id.progressBar);
        btnRefresh = findViewById(R.id.btnRefresh);
        barChart = findViewById(R.id.barChart);
        tabLayout = findViewById(R.id.tabLayout);

        if (getIntent().hasExtra("EXTRA_FRANCHISEE_ID")) {
            setTitle("Analytics: " + franchiseeId);
        }

        setupChart();
        btnRefresh.setOnClickListener(v -> loadAnalytics());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateChart(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        
        setupSpinners();
    }

    private void setupSpinners() {
        spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateDayAnalysis(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateWeekAnalysis(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMonthAnalysis(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupChart() {
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);
        barChart.setMaxVisibleValueCount(60);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);
        barChart.setExtraOffsets(5f, 5f, 5f, 30f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-90f);

        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(true);
    }

    private void loadAnalytics() {
        if (franchiseeId == null || franchiseeId.isEmpty()) return;
        
        progressBar.setVisibility(View.VISIBLE);
        String authHeader = "Bearer " + accessToken;
        String filter = "eq." + franchiseeId;

        supabaseService.getDispenseData(authHeader, "*", filter).enqueue(new Callback<List<DispenseData>>() {
            @Override
            public void onResponse(Call<List<DispenseData>> call, Response<List<DispenseData>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allData = response.body();
                    populateSpinners();
                    updateChart(tabLayout.getSelectedTabPosition());
                }
            }
            @Override
            public void onFailure(Call<List<DispenseData>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void populateSpinners() {
        Set<String> days = new HashSet<>();
        Set<String> weeks = new HashSet<>();
        Set<String> months = new HashSet<>();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();

        for (DispenseData data : allData) {
            days.add(data.entryDate);
            months.add(data.entryDate.substring(0, 7)); // yyyy-MM
            try {
                Date d = sdf.parse(data.entryDate);
                cal.setTime(d);
                int week = cal.get(Calendar.WEEK_OF_YEAR);
                int year = cal.get(Calendar.YEAR);
                weeks.add(year + "-W" + (week < 10 ? "0" + week : week));
            } catch (Exception e) {}
        }

        // Proper sorting for Spinner items (Descending order)
        List<String> sortedDays = new ArrayList<>(days);
        Collections.sort(sortedDays, Collections.reverseOrder());
        updateSpinner(spinnerDay, sortedDays);

        List<String> sortedWeeks = new ArrayList<>(weeks);
        Collections.sort(sortedWeeks, Collections.reverseOrder());
        updateSpinner(spinnerWeek, sortedWeeks);

        List<String> sortedMonths = new ArrayList<>(months);
        Collections.sort(sortedMonths, Collections.reverseOrder());
        updateSpinner(spinnerMonth, sortedMonths);
    }

    private void updateSpinner(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void updateDayAnalysis(String selectedDay) {
        long indent = 0, dispense = 0;
        for (DispenseData data : allData) {
            if (data.entryDate.equals(selectedDay)) {
                indent += data.indentAmount;
                dispense += data.dispenseAmount;
            }
        }
        updateUI(tvDayStats, tvDayPercent, indent, dispense);
    }

    private void updateWeekAnalysis(String selectedWeek) {
        long indent = 0, dispense = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        
        for (DispenseData data : allData) {
            try {
                Date d = sdf.parse(data.entryDate);
                cal.setTime(d);
                int weekNum = cal.get(Calendar.WEEK_OF_YEAR);
                int yearNum = cal.get(Calendar.YEAR);
                String weekKey = yearNum + "-W" + (weekNum < 10 ? "0" + weekNum : weekNum);
                
                if (weekKey.equals(selectedWeek)) {
                    indent += data.indentAmount;
                    dispense += data.dispenseAmount;
                }
            } catch (Exception e) {}
        }
        updateUI(tvWeekStats, tvWeekPercent, indent, dispense);
    }

    private void updateMonthAnalysis(String selectedMonth) {
        long indent = 0, dispense = 0;
        for (DispenseData data : allData) {
            if (data.entryDate.startsWith(selectedMonth)) {
                indent += data.indentAmount;
                dispense += data.dispenseAmount;
            }
        }
        updateUI(tvMonthStats, tvMonthPercent, indent, dispense);
    }

    private void updateUI(TextView statsView, TextView percentView, long indent, long dispense) {
        statsView.setText(String.format(Locale.US, "Indent: ₹%d | Dispense: ₹%d", indent, dispense));
        if (indent > 0) {
            double percent = (double) dispense / indent * 100;
            percentView.setText(String.format(Locale.US, "Performance: %.2f%%", percent));
            if (percent < 80) percentView.setTextColor(0xFFD32F2F);
            else if (percent < 95) percentView.setTextColor(0xFFFF9800);
            else percentView.setTextColor(0xFF4CAF50);
        } else {
            percentView.setText("Performance: N/A");
            percentView.setTextColor(0xFF757575);
        }
    }

    private void updateChart(int position) {
        if (allData.isEmpty()) return;

        Map<String, Integer> groupedData = new TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();

        switch (position) {
            case 0: // Daily
                for (DispenseData data : allData) groupedData.put(data.entryDate, data.numTransactions);
                if (groupedData.size() > 10) {
                    List<String> keys = new ArrayList<>(groupedData.keySet());
                    for (int i = 0; i < keys.size() - 10; i++) groupedData.remove(keys.get(i));
                }
                break;
            case 1: // Weekly
                for (DispenseData data : allData) {
                    try {
                        Date d = sdf.parse(data.entryDate);
                        cal.setTime(d);
                        String key = cal.get(Calendar.YEAR) + "-W" + cal.get(Calendar.WEEK_OF_YEAR);
                        groupedData.put(key, groupedData.getOrDefault(key, 0) + data.numTransactions);
                    } catch (Exception e) {}
                }
                break;
            case 2: // Monthly
                for (DispenseData data : allData) {
                    String key = data.entryDate.substring(0, 7);
                    groupedData.put(key, groupedData.getOrDefault(key, 0) + data.numTransactions);
                }
                break;
            case 3: // Quarterly
                for (DispenseData data : allData) {
                    try {
                        int m = Integer.parseInt(data.entryDate.substring(5, 7));
                        String key = data.entryDate.substring(0, 4) + "-Q" + ((m - 1) / 3 + 1);
                        groupedData.put(key, groupedData.getOrDefault(key, 0) + data.numTransactions);
                    } catch (Exception e) {}
                }
                break;
        }

        List<BarEntry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>(groupedData.keySet());
        int i = 0;
        for (String label : labels) entries.add(new BarEntry(i++, groupedData.get(label)));

        BarDataSet set = new BarDataSet(entries, "Transactions");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData data = new BarData(set);
        data.setBarWidth(labels.size() > 20 ? 0.4f : labels.size() > 10 ? 0.6f : 0.85f);
        
        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    String label = labels.get(index);
                    return (position == 0 && label.length() >= 10) ? label.substring(5) : label.length() > 5 ? label.substring(2) : label;
                }
                return "";
            }
        });
        barChart.setFitBars(true);
        barChart.animateY(1000);
        barChart.invalidate();
    }
}
