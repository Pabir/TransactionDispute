package com.example.transactiondispute;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.example.transactiondispute.api.SupabaseService;
import com.example.transactiondispute.cashmanagementapp.CashmanagementActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

class Transaction {
    private String atmId;
    private String transDate;
    private String time;
    private String transactionId;
    private String utr;
    private String customerName;
    private String customerMobileNumber;
    private String bankName;
    private String disputeAmount;
    private String transactionType;

    public Transaction(String atmId, String transDate, String time, String transactionId, 
                      String utr, String customerName, String customerMobileNumber, String bankName, 
                      String disputeAmount, String transactionType) {
        this.atmId = atmId;
        this.transDate = transDate;
        this.time = time;
        this.transactionId = transactionId;
        this.utr = utr;
        this.customerName = customerName;
        this.customerMobileNumber = customerMobileNumber;
        this.bankName = bankName;
        this.disputeAmount = disputeAmount;
        this.transactionType = transactionType;
    }

    public Map<String, Object> toMap(String userId, String franchiseeId) {
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", userId);
        map.put("franchisee_id", franchiseeId);
        map.put("atm_id", atmId);
        map.put("trans_date", transDate);
        map.put("trans_time", time);
        map.put("transaction_id", transactionId);
        map.put("utr", utr);
        map.put("customer_name", customerName);
        map.put("customer_mobile_number", customerMobileNumber);
        map.put("bank_name", bankName);
        try {
            map.put("dispute_amount", Double.parseDouble(disputeAmount));
        } catch (Exception e) {
            map.put("dispute_amount", 0.0);
        }
        map.put("transaction_type", transactionType);
        return map;
    }

    // Getters
    public String getAtmId() { return atmId; }
    public String getCustomerName() { return customerName; }
    public String getDisputeAmount() { return disputeAmount; }
    public String getTransactionType() { return transactionType; }
    public String getTransDate() { return transDate; }
    public String getTime() { return time; }
    public String getTransactionId() { return transactionId; }
    public String getUtr() { return utr; }
    public String getCustomerMobileNumber() { return customerMobileNumber; }
    public String getBankName() { return bankName; }
}

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etAtmId, etTransDate, etTime, etTransactionId, etUtr, etCustomerName, etCustomerMobileNumber, etBankName, etDisputeAmount;
    private RadioGroup rgTransactionType;
    private Button btnAddTransaction, btnExport, btnShareWhatsApp, btnSendEmail, btnLogout;
    private TextView tvTransactions;
    
    private List<Transaction> transactionList;
    private SupabaseService supabaseService;
    private String accessToken, userId, franchiseeId;
    
    private File currentExportedFile;
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int MANAGE_STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadSession();
        initRetrofit();
        initializeViews();
        transactionList = new ArrayList<>();
        
        btnAddTransaction.setOnClickListener(v -> addTransaction());
        btnExport.setOnClickListener(v -> checkPermissionAndExport());
        btnShareWhatsApp.setOnClickListener(v -> shareToWhatsApp());
        btnSendEmail.setOnClickListener(v -> sendEnhancedEmail());
        btnLogout.setOnClickListener(v -> logout());
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

    private void addTransaction() {
        String atmId = etAtmId.getText().toString().trim();
        String transDate = etTransDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String transactionId = etTransactionId.getText().toString().trim();
        String utr = etUtr.getText().toString().trim();
        String customerName = etCustomerName.getText().toString().trim();
        String customerMobileNumber = etCustomerMobileNumber.getText().toString().trim();
        String bankName = etBankName.getText().toString().trim();
        String disputeAmount = etDisputeAmount.getText().toString().trim();
        
        String transactionType = "";
        int selectedId = rgTransactionType.getCheckedRadioButtonId();
        if (selectedId == R.id.rbWithdrawal) transactionType = "WITHDRAWAL";
        else if (selectedId == R.id.rbDeposit) transactionType = "DEPOSIT";

        if (atmId.isEmpty() || transDate.isEmpty() || transactionId.isEmpty() || transactionType.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Transaction transaction = new Transaction(atmId, transDate, time, transactionId, 
                utr, customerName, customerMobileNumber, bankName, disputeAmount, transactionType);
        
        saveToSupabase(transaction);
    }

    private void saveToSupabase(Transaction transaction) {
        btnAddTransaction.setEnabled(false);
        String authHeader = "Bearer " + accessToken;
        
        supabaseService.insertTransaction(authHeader, transaction.toMap(userId, franchiseeId))
                .enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnAddTransaction.setEnabled(true);
                if (response.isSuccessful()) {
                    transactionList.add(transaction);
                    updateTransactionsView();
                    clearForm();
                    Toast.makeText(MainActivity.this, "Saved to Cloud!", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Cloud sync failed (" + response.code() + ")";
                    if (response.code() == 401 || response.code() == 403) {
                        errorMsg = "Session expired or access denied. Please re-login.";
                    }
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    
                    // Fallback: save locally anyway so they can still export it
                    transactionList.add(transaction);
                    updateTransactionsView();
                    clearForm();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnAddTransaction.setEnabled(true);
                Toast.makeText(MainActivity.this, "Network error. Saved locally.", Toast.LENGTH_SHORT).show();
                transactionList.add(transaction);
                updateTransactionsView();
                clearForm();
            }
        });
    }

    private void logout() {
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void initializeViews() {
        etAtmId = findViewById(R.id.etAtmId);
        etTransDate = findViewById(R.id.etTransDate);
        etTime = findViewById(R.id.etTime);
        etTransactionId = findViewById(R.id.etTransactionId);
        etUtr = findViewById(R.id.etUtr);
        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerMobileNumber = findViewById(R.id.etCustomerMobileNumber);
        etBankName = findViewById(R.id.etBankName);
        etDisputeAmount = findViewById(R.id.etDisputeAmount);
        rgTransactionType = findViewById(R.id.rgTransactionType);
        btnAddTransaction = findViewById(R.id.btnAddTransaction);
        btnExport = findViewById(R.id.btnExport);
        btnShareWhatsApp = findViewById(R.id.btnShareWhatsApp);
        tvTransactions = findViewById(R.id.tvTransactions);
        btnSendEmail = findViewById(R.id.btnSendEmail);
        btnLogout = findViewById(R.id.btnLogout);
        
        btnShareWhatsApp.setEnabled(false);
        btnSendEmail.setEnabled(false);
    }

    private void updateTransactionsView() {
        if (transactionList.isEmpty()) {
            tvTransactions.setText("No transactions added");
            btnShareWhatsApp.setEnabled(false);
            btnSendEmail.setEnabled(false);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Added Transactions:\n\n");
        for (int i = 0; i < transactionList.size(); i++) {
            Transaction t = transactionList.get(i);
            sb.append("ID: ").append(t.getTransactionId()).append(" | ₹").append(t.getDisputeAmount()).append("\n");
        }
        tvTransactions.setText(sb.toString());
        btnShareWhatsApp.setEnabled(true);
        btnSendEmail.setEnabled(true);
    }

    private void clearForm() {
        etAtmId.setText(""); etTransDate.setText(""); etTime.setText("");
        etTransactionId.setText(""); etUtr.setText(""); etCustomerName.setText("");
        etCustomerMobileNumber.setText(""); etBankName.setText(""); etDisputeAmount.setText("");
        rgTransactionType.clearCheck();
    }

    // Storage and Export Logic
    private void checkPermissionAndExport() {
        if (transactionList.isEmpty()) {
            Toast.makeText(this, "No transactions to export", Toast.LENGTH_SHORT).show();
            return;
        }
        exportToExcel();
    }

    private void exportToExcel() {
        try {
            StringBuilder csvContent = new StringBuilder("ATM ID,Trans Date,Time,Transaction ID,UTR,Customer Name,Customer Mobile,Bank Name,Dispute Amount,Type\n");
            for (Transaction t : transactionList) {
                csvContent.append(t.getAtmId()).append(",").append(t.getTransDate()).append(",").append(t.getTime()).append(",")
                          .append(t.getTransactionId()).append(",").append(t.getUtr()).append(",").append(t.getCustomerName()).append(",")
                          .append(t.getCustomerMobileNumber()).append(",").append(t.getBankName()).append(",").append(t.getDisputeAmount()).append(",")
                          .append(t.getTransactionType()).append("\n");
            }
            String fileName = "Transactions_" + System.currentTimeMillis() + ".csv";
            
            // Use app-specific internal directory to avoid permission issues on Android 11+
            File file = new File(getExternalFilesDir(null), fileName);
            
            FileOutputStream out = new FileOutputStream(file);
            out.write(csvContent.toString().getBytes());
            out.close();
            
            currentExportedFile = file;
            Toast.makeText(this, "Exported Successfully!", Toast.LENGTH_SHORT).show();
            
            // Automatically enable buttons
            btnShareWhatsApp.setEnabled(true);
            btnSendEmail.setEnabled(true);
        } catch (IOException e) {
            Toast.makeText(this, "Export Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareToWhatsApp() {
        if (currentExportedFile == null) {
            Toast.makeText(this, "Please export first", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", currentExportedFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share via"));
        } catch (Exception e) {
            Toast.makeText(this, "Sharing failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEnhancedEmail() {
        if (currentExportedFile == null) {
            Toast.makeText(this, "Please export first", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            
            String[] to = {"WLASupport@hitachi-payments.com"};
            String[] cc = {"mf.techeasyservices@gmail.com", "dibyendu.majumder@hitachi-payments.com"};
            
            intent.putExtra(Intent.EXTRA_EMAIL, to);
            intent.putExtra(Intent.EXTRA_CC, cc);

            intent.putExtra(Intent.EXTRA_SUBJECT, "Transaction Dispute Report - " + franchiseeId);
            intent.putExtra(Intent.EXTRA_TEXT, createEmailBody());
            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, getPackageName() + ".provider", currentExportedFile));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (Exception e) {
            Toast.makeText(this, "Email failed", Toast.LENGTH_SHORT).show();
        }
    }

    private String createEmailBody() {
        StringBuilder body = new StringBuilder();
        body.append("Dear Support Team,\n\n");
        body.append("I am reporting ").append(transactionList.size()).append(" transaction dispute(s).\n\n");
        body.append("DETAILED TRANSACTIONS:\n");
        
        for (int i = 0; i < transactionList.size(); i++) {
            Transaction t = transactionList.get(i);
            body.append("\nTransaction ").append(i + 1).append(":\n");
            body.append("  • ATM ID: ").append(t.getAtmId()).append("\n");
            body.append("  • Transaction Date: ").append(t.getTransDate()).append("\n");
            body.append("  • Time: ").append(t.getTime()).append("\n");
            body.append("  • Transaction ID: ").append(t.getTransactionId()).append("\n");
            body.append("  • UTR: ").append(t.getUtr()).append("\n");
            body.append("  • Customer Name: ").append(t.getCustomerName()).append("\n");
            body.append("  • Customer Mobile: ").append(t.getCustomerMobileNumber()).append("\n");
            body.append("  • Bank Name: ").append(t.getBankName()).append("\n");
            body.append("  • Dispute Amount: ₹").append(t.getDisputeAmount()).append("\n");
            body.append("  • Transaction Type: ").append(t.getTransactionType()).append("\n");
        }
        
        body.append("\nPlease find the detailed report attached in CSV format.\n\n");
        body.append("Best regards,\n");
        body.append("Franchisee ID: ").append(franchiseeId);
        
        return body.toString();
    }
}
