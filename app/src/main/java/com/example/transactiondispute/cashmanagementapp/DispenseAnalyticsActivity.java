package com.example.transactiondispute.cashmanagementapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        franchiseeId = prefs.getString("franchisee_id", "");
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
        progressBar = findViewById(R.id.progressBar);
        btnRefresh = findViewById(R.id.btnRefresh);
        barChart = findViewById(R.id.barChart);
        tabLayout = findViewById(R.id.tabLayout);

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
    }

    private void setupChart() {
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);
        barChart.setMaxVisibleValueCount(60);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);

        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(true);
    }

    private void loadAnalytics() {
        progressBar.setVisibility(View.VISIBLE);
        String authHeader = "Bearer " + accessToken;
        String filter = "eq." + franchiseeId;

        supabaseService.getDispenseData(authHeader, "*", filter).enqueue(new Callback<List<DispenseData>>() {
            @Override
            public void onResponse(Call<List<DispenseData>> call, Response<List<DispenseData>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allData = response.body();
                    calculateAnalytics(allData);
                    updateChart(tabLayout.getSelectedTabPosition());
                } else {
                    Toast.makeText(DispenseAnalyticsActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DispenseData>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DispenseAnalyticsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateChart(int position) {
        if (allData.isEmpty()) return;

        Map<String, Integer> groupedData = new TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();

        switch (position) {
            case 0: // Daily (Last 10 days)
                for (DispenseData data : allData) {
                    groupedData.put(data.entryDate, data.numTransactions);
                }
                // Keep only last 10
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
                        int week = cal.get(Calendar.WEEK_OF_YEAR);
                        int year = cal.get(Calendar.YEAR);
                        String key = year + "-W" + week;
                        groupedData.put(key, groupedData.getOrDefault(key, 0) + data.numTransactions);
                    } catch (Exception e) {}
                }
                break;

            case 2: // Monthly
                for (DispenseData data : allData) {
                    String key = data.entryDate.substring(0, 7); // yyyy-MM
                    groupedData.put(key, groupedData.getOrDefault(key, 0) + data.numTransactions);
                }
                break;

            case 3: // Quarterly
                for (DispenseData data : allData) {
                    try {
                        String month = data.entryDate.substring(5, 7);
                        int m = Integer.parseInt(month);
                        int q = (m - 1) / 3 + 1;
                        String key = data.entryDate.substring(0, 4) + "-Q" + q;
                        groupedData.put(key, groupedData.getOrDefault(key, 0) + data.numTransactions);
                    } catch (Exception e) {}
                }
                break;
        }

        List<BarEntry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>(groupedData.keySet());
        int i = 0;
        for (String label : labels) {
            entries.add(new BarEntry(i++, groupedData.get(label)));
        }

        BarDataSet set = new BarDataSet(entries, "Transactions");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        set.setValueTextSize(10f);

        BarData data = new BarData(set);
        barChart.setData(data);
        
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    String label = labels.get(index);
                    if (label.length() > 5) return label.substring(2); // Shorten for display
                    return label;
                }
                return "";
            }
        });

        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void calculateAnalytics(List<DispenseData> dataList) {
        if (dataList.isEmpty()) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String today = sdf.format(new Date());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        Date weekAgo = cal.getTime();

        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -30);
        Date monthAgo = cal.getTime();

        long dayIndent = 0, dayDispense = 0;
        long weekIndent = 0, weekDispense = 0;
        long monthIndent = 0, monthDispense = 0;

        for (DispenseData data : dataList) {
            try {
                Date entryDate = sdf.parse(data.entryDate);
                
                if (data.entryDate.equals(today)) {
                    dayIndent += data.indentAmount;
                    dayDispense += data.dispenseAmount;
                }

                if (entryDate.after(weekAgo)) {
                    weekIndent += data.indentAmount;
                    weekDispense += data.dispenseAmount;
                }

                if (entryDate.after(monthAgo)) {
                    monthIndent += data.indentAmount;
                    monthDispense += data.dispenseAmount;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        updateUI(tvDayStats, tvDayPercent, dayIndent, dayDispense);
        updateUI(tvWeekStats, tvWeekPercent, weekIndent, weekDispense);
        updateUI(tvMonthStats, tvMonthPercent, monthIndent, monthDispense);
    }

    private void updateUI(TextView statsView, TextView percentView, long indent, long dispense) {
        statsView.setText(String.format(Locale.US, "Indent: ₹%d | Dispense: ₹%d", indent, dispense));
        if (indent > 0) {
            double percent = (double) dispense / indent * 100;
            percentView.setText(String.format(Locale.US, "Performance: %.2f%%", percent));
            
            if (percent < 80) percentView.setTextColor(0xFFD32F2F); // Red
            else if (percent < 95) percentView.setTextColor(0xFFFF9800); // Orange
            else percentView.setTextColor(0xFF4CAF50); // Green
        } else {
            percentView.setText("Performance: N/A");
            percentView.setTextColor(0xFF757575);
        }
    }
}
