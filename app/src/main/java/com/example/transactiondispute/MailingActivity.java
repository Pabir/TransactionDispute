package com.example.transactiondispute;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.LinearLayout;
import com.example.transactiondispute.cashmanagementapp.CashmanagementActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MailingActivity extends AppCompatActivity {
    
    private Button btnMachineIssues, btnUpsBattery, btnNetworkLink, btnRoomInfrastructure, btnEodDocketRequest;
    private Button btnTransactionDisputes, btnCashManagement, btnOpenGmail;
    private TextView tvEmailStatus;
    private String franchiseeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Session Validation
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        franchiseeId = prefs.getString("franchisee_id", "Unknown");
        
        setContentView(R.layout.activity_mailing);
        initializeViews();
        setupEventListeners();
    }
    
    private void initializeViews() {
        btnMachineIssues = findViewById(R.id.btnMachineIssues);
        btnUpsBattery = findViewById(R.id.btnUpsBattery);
        btnNetworkLink = findViewById(R.id.btnNetworkLink);
        btnRoomInfrastructure = findViewById(R.id.btnRoomInfrastructure);
        btnTransactionDisputes = findViewById(R.id.btnTransactionDisputes);
        btnCashManagement = findViewById(R.id.btnCashManagement);
        btnOpenGmail = findViewById(R.id.btnOpenGmail);
        tvEmailStatus = findViewById(R.id.tvEmailStatus);
        btnEodDocketRequest = findViewById(R.id.btnEodDocketRequest);
    }
    
    private void setupEventListeners() {
        btnMachineIssues.setOnClickListener(v -> showMachineIssueDialog());
        btnEodDocketRequest.setOnClickListener(v -> showAtmIdDialogForEod());
        btnTransactionDisputes.setOnClickListener(v -> startActivity(new Intent(this, TransactionDisputeActivity.class)));
        btnCashManagement.setOnClickListener(v -> startActivity(new Intent(this, CashmanagementActivity.class)));
        btnOpenGmail.setOnClickListener(v -> openGmailApp());
    }

    private void showAtmIdDialogForEod() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("EOD Docket Request");
        final EditText input = new EditText(this);
        input.setHint("Enter ATM ID");
        builder.setView(input);
        builder.setPositiveButton("Send", (dialog, which) -> {
            String atmId = input.getText().toString().trim();
            if (!atmId.isEmpty()) sendEodEmail(atmId);
        });
        builder.show();
    }

    private void sendEodEmail(String atmId) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"mf.teacheasyservices@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "EOD Request - " + atmId + " - " + franchiseeId);
        intent.putExtra(Intent.EXTRA_TEXT, "Requesting EOD for ATM: " + atmId + "\nFranchisee: " + franchiseeId);
        startActivity(Intent.createChooser(intent, "Send Email"));
    }

    private void showMachineIssueDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Report Machine Issue");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        final EditText etAtmId = new EditText(this);
        etAtmId.setHint("ATM ID");
        layout.addView(etAtmId);

        final Spinner spIssue = new Spinner(this);
        spIssue.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, 
            new String[]{"Note Jam", "Card Reader Error", "Cassette Disabled"}));
        layout.addView(spIssue);

        builder.setView(layout);
        builder.setPositiveButton("Report", (dialog, which) -> {
            String atmId = etAtmId.getText().toString().trim();
            if (!atmId.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Issue: " + spIssue.getSelectedItem() + " at " + atmId);
                intent.putExtra(Intent.EXTRA_TEXT, "Franchisee: " + franchiseeId + "\nATM: " + atmId);
                startActivity(Intent.createChooser(intent, "Send Report"));
            }
        });
        builder.show();
    }

    private void openGmailApp() {
        try {
            startActivity(getPackageManager().getLaunchIntentForPackage("com.google.android.gm"));
        } catch (Exception e) {
            Toast.makeText(this, "Gmail not found", Toast.LENGTH_SHORT).show();
        }
    }
}
