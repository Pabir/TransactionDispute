package com.example.transactiondispute.cashmanagementapp;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class SimpleBarChart extends View {
    private List<Float> values = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private Paint barPaint, textPaint;
    private float maxValue = 100f;
    
    public SimpleBarChart(Context context) {
        super(context);
        init();
    }
    
    public SimpleBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        barPaint = new Paint();
        barPaint.setColor(Color.parseColor("#4CAF50"));
        barPaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }
    
    public void setData(List<Float> values, List<String> labels) {
        this.values = new ArrayList<>(values);
        this.labels = new ArrayList<>(labels);
        
        // Find max value for scaling
        maxValue = 0f;
        for (float value : values) {
            if (value > maxValue) maxValue = value;
        }
        if (maxValue == 0f) maxValue = 100f;
        
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (values.isEmpty()) return;
        
        int width = getWidth();
        int height = getHeight();
        int barWidth = width / values.size() - 20;
        
        for (int i = 0; i < values.size(); i++) {
            float value = values.get(i);
            float barHeight = (value / maxValue) * (height - 100);
            
            float left = i * (barWidth + 20) + 10;
            float top = height - barHeight - 50;
            float right = left + barWidth;
            float bottom = height - 50;
            
            // Draw bar
            canvas.drawRect(left, top, right, bottom, barPaint);
            
            // Draw value text
            canvas.drawText(String.valueOf((int)value), 
                          left + barWidth/2, top - 10, textPaint);
            
            // Draw label
            canvas.drawText(labels.get(i), 
                          left + barWidth/2, height - 20, textPaint);
        }
    }
}