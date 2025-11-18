package com.example.transactiondispute.cashmanagementapp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.transactiondispute.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {
    
    private LinearLayout chartsContainer;
    private DataRepository dataRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        
        chartsContainer = findViewById(R.id.chartsContainer);
        dataRepository = new DataRepository(this); // Fixed: added 'this' context
        
        loadSummaryData();
        createSimpleCharts();
    }
    
    private void loadSummaryData() {
        List<CashData> dataList = dataRepository.getCashData(); // Use real data instead of sample
        
        int totalLoad = 0;
        int totalIndent = 0;
        int total500 = 0;
        int total200 = 0;
        int total100 = 0;
        int totalEod = 0;
        
        for (CashData data : dataList) {
            totalLoad += data.getSumLoadAmount();
            totalIndent += data.getIndentAmount();
            total500 += data.getSum500Notes();
            total200 += data.getSum200Notes();
            total100 += data.getSum100Notes();
            totalEod += data.getSumEodAmount();
        }
        
        // Update summary TextViews
        TextView tvTotalLoad = findViewById(R.id.tvTotalLoadSummary);
        TextView tvTotalIndent = findViewById(R.id.tvTotalIndentSummary);
        TextView tvTotalEod = findViewById(R.id.tvTotalEodSummary);
        
        tvTotalLoad.setText("Total Load: ₹" + totalLoad);
        tvTotalIndent.setText("Total Indent: ₹" + totalIndent);
        tvTotalEod.setText("Total EOD: ₹" + totalEod);
    }
    
    private void createSimpleCharts() {
        List<CashData> dataList = dataRepository.getCashData(); // Use real data instead of sample
        
        // Create note distribution chart
        createNoteDistributionChart(dataList);
        
        // Create amount comparison chart
        createAmountComparisonChart(dataList);
    }
    
    private void createNoteDistributionChart(List<CashData> dataList) {
        CardView cardView = new CardView(this);
        cardView.setCardElevation(4f);
        cardView.setRadius(8f);
        cardView.setContentPadding(16, 16, 16, 16);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        
        TextView title = new TextView(this);
        title.setText("Note Distribution");
        title.setTextSize(18f);
        layout.addView(title);
        
        // Calculate totals
        int total500 = 0, total200 = 0, total100 = 0;
        for (CashData data : dataList) {
            total500 += data.getSum500Notes();
            total200 += data.getSum200Notes();
            total100 += data.getSum100Notes();
        }
        
        // Create simple text-based chart
        addBarToLayout(layout, "500 Notes", total500, "#4CAF50");
        addBarToLayout(layout, "200 Notes", total200, "#2196F3");
        addBarToLayout(layout, "100 Notes", total100, "#FF9800");
        
        cardView.addView(layout);
        chartsContainer.addView(cardView);
    }
    
    private void createAmountComparisonChart(List<CashData> dataList) {
        CardView cardView = new CardView(this);
        cardView.setCardElevation(4f);
        cardView.setRadius(8f);
        cardView.setContentPadding(16, 16, 16, 16);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        
        TextView title = new TextView(this);
        title.setText("Amount Comparison");
        title.setTextSize(18f);
        layout.addView(title);
        
        // Calculate totals
        int totalLoad = 0, totalIndent = 0, totalEod = 0;
        for (CashData data : dataList) {
            totalLoad += data.getSumLoadAmount();
            totalIndent += data.getIndentAmount();
            totalEod += data.getSumEodAmount();
        }
        
        addBarToLayout(layout, "Load Amount", totalLoad/1000, "#4CAF50");
        addBarToLayout(layout, "Indent Amount", totalIndent/1000, "#2196F3");
        addBarToLayout(layout, "EOD Amount", totalEod/1000, "#FF9800");
        
        TextView note = new TextView(this);
        note.setText("(Amounts in thousands)");
        note.setTextSize(12f);
        note.setPadding(0, 8, 0, 0);
        layout.addView(note);
        
        cardView.addView(layout);
        chartsContainer.addView(cardView);
    }
    
    private void addBarToLayout(LinearLayout layout, String label, int value, String color) {
        LinearLayout barLayout = new LinearLayout(this);
        barLayout.setOrientation(LinearLayout.HORIZONTAL);
        barLayout.setPadding(0, 8, 0, 8);
        
        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setWidth(200);
        labelView.setTextSize(14f);
        
        TextView valueView = new TextView(this);
        valueView.setText(String.valueOf(value));
        valueView.setTextSize(14f);
        valueView.setPadding(16, 0, 0, 0);
        valueView.setTextColor(Color.parseColor(color));
        
        barLayout.addView(labelView);
        barLayout.addView(valueView);
        layout.addView(barLayout);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        loadSummaryData();
        createSimpleCharts();
    }
}