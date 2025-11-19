package com.example.transactiondispute;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MailingActivity extends AppCompatActivity {
    
    private Button btnMachineIssues, btnUpsBattery, btnNetworkLink, btnRoomInfrastructure;
    private Button btnTransactionDisputes, btnCashManagement, btnOpenGmail;
    private TextView tvEmailStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }
    
    private void setupEventListeners() {
        btnMachineIssues.setOnClickListener(v -> sendMachineIssuesEmail());
        btnUpsBattery.setOnClickListener(v -> sendUpsBatteryEmail());
        btnNetworkLink.setOnClickListener(v -> sendNetworkLinkEmail());
        btnRoomInfrastructure.setOnClickListener(v -> sendRoomInfrastructureEmail());
        btnTransactionDisputes.setOnClickListener(v -> sendTransactionDisputesEmail());
        btnCashManagement.setOnClickListener(v -> sendCashManagementEmail());
        btnOpenGmail.setOnClickListener(v -> openGmailApp());
    }
    
    private void sendMachineIssuesEmail() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            
            String[] to = {"support@hitachi-payments.com", "technicalsupport@hitachi-payments.com"};
            String[] cc = {"manager@hitachi-payments.com"};
            
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_CC, cc);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ATM Machine Issue - " + getCurrentDate());
            emailIntent.putExtra(Intent.EXTRA_TEXT, createMachineIssuesContent());
            
            startActivity(Intent.createChooser(emailIntent, "Send Machine Issue Report..."));
            tvEmailStatus.setText("Machine issue email ready to send");
            
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void sendUpsBatteryEmail() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            
            String[] to = {"electrical@hitachi-payments.com", "support@hitachi-payments.com"};
            String[] cc = {"facility@hitachi-payments.com"};
            
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_CC, cc);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "UPS/Battery Issue - " + getCurrentDate());
            emailIntent.putExtra(Intent.EXTRA_TEXT, createUpsBatteryContent());
            
            startActivity(Intent.createChooser(emailIntent, "Send UPS/Battery Report..."));
            tvEmailStatus.setText("UPS/Battery email ready to send");
            
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void sendNetworkLinkEmail() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            
            String[] to = {"network@hitachi-payments.com", "itsupport@hitachi-payments.com"};
            String[] cc = {"operations@hitachi-payments.com"};
            
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_CC, cc);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Network/Link Issue - " + getCurrentDate());
            emailIntent.putExtra(Intent.EXTRA_TEXT, createNetworkLinkContent());
            
            startActivity(Intent.createChooser(emailIntent, "Send Network Issue Report..."));
            tvEmailStatus.setText("Network issue email ready to send");
            
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void sendRoomInfrastructureEmail() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            
            String[] to = {"facility@hitachi-payments.com", "admin@hitachi-payments.com"};
            String[] cc = {"manager@hitachi-payments.com"};
            
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_CC, cc);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Room/Infrastructure Issue - " + getCurrentDate());
            emailIntent.putExtra(Intent.EXTRA_TEXT, createRoomInfrastructureContent());
            
            startActivity(Intent.createChooser(emailIntent, "Send Infrastructure Report..."));
            tvEmailStatus.setText("Infrastructure email ready to send");
            
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void sendTransactionDisputesEmail() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            
            String[] to = {"WLARecon@hitachi-payments.com", "AppSupport@hitachi-payments.com"};
            String[] cc = {"WLASupport@hitachi-payments.com"};
            
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_CC, cc);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Transaction Dispute - " + getCurrentDate());
            emailIntent.putExtra(Intent.EXTRA_TEXT, createTransactionDisputesContent());
            
            startActivity(Intent.createChooser(emailIntent, "Send Transaction Dispute..."));
            tvEmailStatus.setText("Transaction dispute email ready to send");
            
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void sendCashManagementEmail() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            
            String[] to = {"cashmanagement@hitachi-payments.com", "accounts@hitachi-payments.com"};
            String[] cc = {"operations@hitachi-payments.com"};
            
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_CC, cc);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Cash Management Report - " + getCurrentDate());
            emailIntent.putExtra(Intent.EXTRA_TEXT, createCashManagementContent());
            
            startActivity(Intent.createChooser(emailIntent, "Send Cash Management Report..."));
            tvEmailStatus.setText("Cash management email ready to send");
            
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void openGmailApp() {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
            if (intent != null) {
                startActivity(intent);
            } else {
                Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
                playStoreIntent.setData(Uri.parse("market://details?id=com.google.android.gm"));
                startActivity(playStoreIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open Gmail", Toast.LENGTH_SHORT).show();
        }
    }
    
    private String getCurrentDate() {
        return new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());
    }
    
    // Email Content Templates
    private String createMachineIssuesContent() {
        return "Dear Technical Support Team,\n\n" +
               "I am reporting an issue with the ATM machine at our location.\n\n" +
               "ISSUE DETAILS:\n" +
               "• Machine Model: [Please specify]\n" +
               "• ATM ID: [Please specify]\n" +
               "• Issue Description: [Please describe the problem]\n" +
               "• Error Messages: [If any]\n" +
               "• Time of Occurrence: [Please specify]\n\n" +
               "URGENCY: [High/Medium/Low]\n\n" +
               "Please arrange for technical support at the earliest.\n\n" +
               "Best regards,\n" +
               "ATM Site Manager";
    }
    
    private String createUpsBatteryContent() {
        return "Dear Electrical Support Team,\n\n" +
               "I am reporting an issue with the UPS/Battery system.\n\n" +
               "ISSUE DETAILS:\n" +
               "• UPS Model: [Please specify]\n" +
               "• Battery Backup Duration: [Please specify]\n" +
               "• Issue Description: [Power fluctuations/Battery not charging/etc.]\n" +
               "• Last Maintenance Date: [If known]\n\n" +
               "URGENCY: [High/Medium/Low]\n\n" +
               "Please schedule maintenance or replacement.\n\n" +
               "Best regards,\n" +
               "ATM Site Manager";
    }
    
    private String createNetworkLinkContent() {
        return "Dear Network Support Team,\n\n" +
               "I am reporting connectivity issues with the ATM network.\n\n" +
               "ISSUE DETAILS:\n" +
               "• Connection Type: [Leased Line/3G/4G]\n" +
               "• ISP: [Please specify]\n" +
               "• Issue Description: [No connectivity/Slow connection/Intermittent drops]\n" +
               "• Duration of Issue: [Please specify]\n\n" +
               "URGENCY: [High/Medium/Low]\n\n" +
               "Please investigate and restore connectivity.\n\n" +
               "Best regards,\n" +
               "ATM Site Manager";
    }
    
    private String createRoomInfrastructureContent() {
        return "Dear Facility Management Team,\n\n" +
               "I am reporting an infrastructure issue at the ATM premises.\n\n" +
               "ISSUE DETAILS:\n" +
               "• Issue Type: [AC not working/Cleaning required/Lights not working/etc.]\n" +
               "• Location: [ATM Room/Entry Area/Other]\n" +
               "• Issue Description: [Please describe the problem]\n" +
               "• Severity: [Critical/Moderate/Minor]\n\n" +
               "URGENCY: [High/Medium/Low]\n\n" +
               "Please arrange for necessary maintenance.\n\n" +
               "Best regards,\n" +
               "ATM Site Manager";
    }
    
    private String createTransactionDisputesContent() {
        return "Dear Transaction Support Team,\n\n" +
               "I am writing to report transaction disputes.\n\n" +
               "DISPUTE DETAILS:\n" +
               "• Number of Transactions: [Please specify]\n" +
               "• Total Amount: [Please specify]\n" +
               "• Transaction Dates: [Please specify]\n" +
               "• Issue: [Failed transactions/Amount discrepancies/etc.]\n\n" +
               "REQUEST:\n" +
               "Please investigate and process refunds at the earliest.\n\n" +
               "Best regards,\n" +
               "ATM Site Manager";
    }
    
    private String createCashManagementContent() {
        return "Dear Cash Management Team,\n\n" +
               "Please find the cash management report below:\n\n" +
               "CASH SUMMARY:\n" +
               "• Total Load Amount: [Please specify]\n" +
               "• Total EOD Amount: [Please specify]\n" +
               "• Discrepancies: [If any]\n" +
               "• Notes Count: [500/200/100 notes breakdown]\n\n" +
               "ISSUES (if any):\n" +
               "[Please describe any cash-related issues]\n\n" +
               "Please review and confirm.\n\n" +
               "Best regards,\n" +
               "ATM Site Manager";
    }
}