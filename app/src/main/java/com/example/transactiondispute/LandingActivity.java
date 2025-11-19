package com.example.transactiondispute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.transactiondispute.cashmanagementapp.CashmanagementActivity;
import com.example.transactiondispute.cashmanagementapp.SummaryActivity;
import com.example.transactiondispute.notecounter.NoteCounterActivity;

public class LandingActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "AppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        
        initializeViews();
        setupNavigation();
    }
    
    private void initializeViews() {
        // All buttons will be set up in setupNavigation()
    }
    
    private void setupNavigation() {
        // Transaction Dispute Button
        Button btnTransactionDispute = findViewById(R.id.btnTransactionDispute);
        btnTransactionDispute.setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, MainActivity.class);
            startActivity(intent);
        });
        
        // Cash Management Button
        Button btnCashManagement = findViewById(R.id.btnCashManagement);
        btnCashManagement.setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, CashmanagementActivity.class);
            startActivity(intent);
        });
        
        // Note Counter Button
        Button btnNoteCounter = findViewById(R.id.btnNoteCounter);
        btnNoteCounter.setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, NoteCounterActivity.class);
            startActivity(intent);
        });
        
        // Summary/Reports Button
        Button btnSummary = findViewById(R.id.btnSummary);
        btnSummary.setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, SummaryActivity.class);
            startActivity(intent);
        });
        
        // Mailing Button - ADD THIS
        Button btnMailing = findViewById(R.id.btnMailing);
        btnMailing.setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, MailingActivity.class);
            startActivity(intent);
        });
        // Logout Button
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());
    }
    
    private void logout() {
        // Clear login state
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        // Go back to login activity
        Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        // Prevent going back to login without logging out
        logout();
    }
}