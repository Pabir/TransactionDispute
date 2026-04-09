package com.example.transactiondispute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.example.transactiondispute.api.SupabaseService;
import com.example.transactiondispute.SupabaseConfig;
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

public class CardTransactionActivity extends AppCompatActivity {

    private TextInputEditText etAtmId, etCardNumber, etTransactionDate, etTransactionTime;
    private TextInputEditText etTransactionId, etUtr, etAmount, etCustomerName, etCustomerMobileNumber, etBankName;
    private RadioGroup rgCardType, rgIssueType;
    private Button btnAddCardTransaction, btnExportCard, btnSendEmail;
    private TextView tvCardTransactions;
    
    private List<CardTransaction> cardTransactionList;
    private File currentExportedFile;
    private SupabaseService supabaseService;
    private String accessToken, userId, franchiseeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_transaction);
        
        loadSession();
        initRetrofit();
        initializeViews();
        cardTransactionList = new ArrayList<>();
        
        setupEventListeners();
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
    
    private void initializeViews() {
        etAtmId = findViewById(R.id.etAtmId);
        etCardNumber = findViewById(R.id.etCardNumber);
        etTransactionDate = findViewById(R.id.etTransactionDate);
        etTransactionTime = findViewById(R.id.etTransactionTime);
        etTransactionId = findViewById(R.id.etTransactionId);
        etUtr = findViewById(R.id.etUtr);
        etAmount = findViewById(R.id.etAmount);
        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerMobileNumber = findViewById(R.id.etCustomerMobileNumber);
        etBankName = findViewById(R.id.etBankName);
        
        rgCardType = findViewById(R.id.rgCardType);
        rgIssueType = findViewById(R.id.rgIssueType);
        
        btnAddCardTransaction = findViewById(R.id.btnAddCardTransaction);
        btnExportCard = findViewById(R.id.btnExportCard);
        btnSendEmail = findViewById(R.id.btnSendEmail);
        
        tvCardTransactions = findViewById(R.id.tvCardTransactions);
        
        btnSendEmail.setEnabled(false);
    }
    
    private void setupEventListeners() {
        btnAddCardTransaction.setOnClickListener(v -> addCardTransaction());
        btnExportCard.setOnClickListener(v -> exportCardTransactions());
        btnSendEmail.setOnClickListener(v -> sendCardTransactionEmail());
    }
    
    private void addCardTransaction() {
        String atmId = etAtmId.getText().toString().trim();
        String cardNumber = etCardNumber.getText().toString().trim();
        String transactionDate = etTransactionDate.getText().toString().trim();
        String transactionTime = etTransactionTime.getText().toString().trim();
        String transactionId = etTransactionId.getText().toString().trim();
        String utr = etUtr.getText().toString().trim();
        String amount = etAmount.getText().toString().trim();
        String customerName = etCustomerName.getText().toString().trim();
        String customerMobileNumber = etCustomerMobileNumber.getText().toString().trim();
        String bankName = etBankName.getText().toString().trim();
        
        String cardType = "";
        int selectedCardType = rgCardType.getCheckedRadioButtonId();
        if (selectedCardType == R.id.rbDebitCard) {
            cardType = "DEBIT CARD";
        } else if (selectedCardType == R.id.rbCreditCard) {
            cardType = "CREDIT CARD";
        }
        
        String issueType = "";
        int selectedIssueType = rgIssueType.getCheckedRadioButtonId();
        if (selectedIssueType == R.id.rbTransactionFailed) {
            issueType = "TRANSACTION FAILED";
        } else if (selectedIssueType == R.id.rbAmountNotDispensed) {
            issueType = "AMOUNT NOT DISPENSED";
        }

        if (atmId.isEmpty() || cardNumber.isEmpty() || transactionDate.isEmpty() || 
            transactionTime.isEmpty() || transactionId.isEmpty() || amount.isEmpty() || 
            cardType.isEmpty() || issueType.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        CardTransaction transaction = new CardTransaction(atmId, cardNumber, transactionDate, 
                transactionTime, transactionId, utr, amount, customerName, customerMobileNumber, bankName, cardType, issueType);
        
        saveToSupabase(transaction);
    }

    private void saveToSupabase(CardTransaction transaction) {
        btnAddCardTransaction.setEnabled(false);
        String authHeader = "Bearer " + accessToken;
        
        supabaseService.insertTransaction(authHeader, transaction.toMap(userId, franchiseeId))
                .enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnAddCardTransaction.setEnabled(true);
                if (response.isSuccessful()) {
                    cardTransactionList.add(transaction);
                    updateCardTransactionsView();
                    clearForm();
                    Toast.makeText(CardTransactionActivity.this, "Saved to Cloud!", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Cloud sync failed (" + response.code() + ")";
                    if (response.code() == 401 || response.code() == 403) {
                        errorMsg = "Session expired. Please re-login.";
                    }
                    Toast.makeText(CardTransactionActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    
                    // Fallback
                    cardTransactionList.add(transaction);
                    updateCardTransactionsView();
                    clearForm();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnAddCardTransaction.setEnabled(true);
                Toast.makeText(CardTransactionActivity.this, "Network error. Saved locally.", Toast.LENGTH_SHORT).show();
                cardTransactionList.add(transaction);
                updateCardTransactionsView();
                clearForm();
            }
        });
    }
    
    private void updateCardTransactionsView() {
        if (cardTransactionList.isEmpty()) {
            tvCardTransactions.setText("No card transactions added");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Added Card Transactions:\n\n");
        for (int i = 0; i < cardTransactionList.size(); i++) {
            CardTransaction t = cardTransactionList.get(i);
            sb.append("ID: ").append(t.transactionId).append(" | ₹").append(t.amount).append("\n");
        }
        tvCardTransactions.setText(sb.toString());
    }
    
    private void clearForm() {
        etAtmId.setText("");
        etCardNumber.setText("");
        etTransactionDate.setText("");
        etTransactionTime.setText("");
        etTransactionId.setText("");
        etUtr.setText("");
        etAmount.setText("");
        etCustomerName.setText("");
        etCustomerMobileNumber.setText("");
        etBankName.setText("");
        rgCardType.clearCheck();
        rgIssueType.clearCheck();
    }
    
    private void exportCardTransactions() {
        if (cardTransactionList.isEmpty()) {
            Toast.makeText(this, "No transactions to export", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            StringBuilder csvContent = new StringBuilder("ATM ID,Card Number,Date,Time,Transaction ID,UTR,Amount,Customer Name,Mobile,Bank,Card Type,Issue\n");
            for (CardTransaction t : cardTransactionList) {
                csvContent.append(t.atmId).append(",").append(t.cardNumber).append(",").append(t.transactionDate).append(",")
                          .append(t.transactionTime).append(",").append(t.transactionId).append(",").append(t.utr).append(",")
                          .append(t.amount).append(",").append(t.customerName).append(",").append(t.customerMobileNumber).append(",")
                          .append(t.bankName).append(",").append(t.cardType).append(",").append(t.issueType).append("\n");
            }
            
            String fileName = "CardDisputes_" + System.currentTimeMillis() + ".csv";
            File file = new File(getExternalFilesDir(null), fileName);
            FileOutputStream out = new FileOutputStream(file);
            out.write(csvContent.toString().getBytes());
            out.close();
            
            currentExportedFile = file;
            btnSendEmail.setEnabled(true);
            Toast.makeText(this, "Exported Successfully!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Export Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void sendCardTransactionEmail() {
        if (currentExportedFile == null) {
            Toast.makeText(this, "Please export first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            
            String[] to = {"WLASupport@hitachi-payments.com"};
            String[] cc = {"mf.techeasyservices@gmail.com", "dibyendu.majumder@hitachi-payments.com"};
            
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_CC, cc);
            
            String subject = "Card Transaction Dispute - " + cardTransactionList.get(0).atmId + " - ₹" + cardTransactionList.get(0).amount;
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            
            emailIntent.putExtra(Intent.EXTRA_TEXT, createCardTransactionEmailContent());
            emailIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, getPackageName() + ".provider", currentExportedFile));
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(emailIntent, "Send Card Transaction Dispute..."));
            
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private String createCardTransactionEmailContent() {
        StringBuilder content = new StringBuilder();
        content.append("Dear Support Team,\n\n");
        content.append("I am reporting ").append(cardTransactionList.size()).append(" card transaction dispute(s).\n\n");
        content.append("DETAILED TRANSACTIONS:\n");
        
        for (int i = 0; i < cardTransactionList.size(); i++) {
            CardTransaction t = cardTransactionList.get(i);
            content.append("\nTransaction ").append(i + 1).append(":\n");
            content.append("  • ATM ID: ").append(t.atmId).append("\n");
            content.append("  • Card Number (Last 4): ").append(t.cardNumber).append("\n");
            content.append("  • Transaction Date: ").append(t.transactionDate).append("\n");
            content.append("  • Time: ").append(t.transactionTime).append("\n");
            content.append("  • Transaction ID: ").append(t.transactionId).append("\n");
            content.append("  • UTR: ").append(t.utr).append("\n");
            content.append("  • Amount: ₹").append(t.amount).append("\n");
            content.append("  • Customer Name: ").append(t.customerName).append("\n");
            content.append("  • Customer Mobile: ").append(t.customerMobileNumber).append("\n");
            content.append("  • Bank Name: ").append(t.bankName).append("\n");
            content.append("  • Card Type: ").append(t.cardType).append("\n");
            content.append("  • Issue Type: ").append(t.issueType).append("\n");
        }
        
        content.append("\nPlease find the detailed report attached in CSV format.\n\n");
        content.append("Best regards,\nATM Site Manager");
        return content.toString();
    }
    
    class CardTransaction {
        String atmId, cardNumber, transactionDate, transactionTime, transactionId, utr, amount, customerName, customerMobileNumber, bankName, cardType, issueType;
        
        public CardTransaction(String atmId, String cardNumber, String transactionDate, 
                             String transactionTime, String transactionId, String utr, String amount,
                             String customerName, String customerMobileNumber, String bankName, 
                             String cardType, String issueType) {
            this.atmId = atmId;
            this.cardNumber = cardNumber;
            this.transactionDate = transactionDate;
            this.transactionTime = transactionTime;
            this.transactionId = transactionId;
            this.utr = utr;
            this.amount = amount;
            this.customerName = customerName;
            this.customerMobileNumber = customerMobileNumber;
            this.bankName = bankName;
            this.cardType = cardType;
            this.issueType = issueType;
        }

        public Map<String, Object> toMap(String userId, String franchiseeId) {
            Map<String, Object> map = new HashMap<>();
            map.put("user_id", userId);
            map.put("franchisee_id", franchiseeId);
            map.put("atm_id", atmId);
            map.put("trans_date", transactionDate);
            map.put("trans_time", transactionTime);
            map.put("transaction_id", transactionId);
            map.put("utr", utr);
            map.put("customer_name", customerName);
            map.put("customer_mobile_number", customerMobileNumber);
            map.put("bank_name", bankName);
            try {
                map.put("dispute_amount", Double.parseDouble(amount));
            } catch (Exception e) {
                map.put("dispute_amount", 0.0);
            }
            map.put("transaction_type", "CARD_" + issueType);
            map.put("card_number", cardNumber);
            map.put("card_type", cardType);
            map.put("issue_type", issueType);
            return map;
        }
    }
}