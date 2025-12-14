package com.example.transactiondispute;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class TransactionDisputeActivity extends AppCompatActivity {

    private Button btnUpiDispute, btnCardDispute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_dispute);
        
        initializeViews();
        setupEventListeners();
    }
    
    private void initializeViews() {
        btnUpiDispute = findViewById(R.id.btnUpiDispute);
        btnCardDispute = findViewById(R.id.btnCardDispute);
    }
    
    private void setupEventListeners() {
        // UPI Transaction Dispute Button
        btnUpiDispute.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionDisputeActivity.this, MainActivity.class);
            intent.putExtra("dispute_type", "UPI");
            startActivity(intent);
        });
        
        // Card Transaction Issue Button
        btnCardDispute.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionDisputeActivity.this, CardTransactionActivity.class);
            startActivity(intent);
        });
    }
}