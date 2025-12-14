package com.example.transactiondispute;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CardTransactionActivity extends AppCompatActivity {

    private EditText etAtmId, etCardNumber, etTransactionDate, etTransactionTime;
    private EditText etTransactionId, etAmount, etCustomerName, etBankName;
    private RadioGroup rgCardType, rgIssueType;
    private Button btnAddCardTransaction, btnExportCard, btnSendEmail;
    private TextView tvCardTransactions;
    
    private List<CardTransaction> cardTransactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_transaction);
        
        initializeViews();
        cardTransactionList = new ArrayList<>();
        
        setupEventListeners();
    }
    
    private void initializeViews() {
        etAtmId = findViewById(R.id.etAtmId);
        etCardNumber = findViewById(R.id.etCardNumber);
        etTransactionDate = findViewById(R.id.etTransactionDate);
        etTransactionTime = findViewById(R.id.etTransactionTime);
        etTransactionId = findViewById(R.id.etTransactionId);
        etAmount = findViewById(R.id.etAmount);
        etCustomerName = findViewById(R.id.etCustomerName);
        etBankName = findViewById(R.id.etBankName);
        
        rgCardType = findViewById(R.id.rgCardType);
        rgIssueType = findViewById(R.id.rgIssueType);
        
        btnAddCardTransaction = findViewById(R.id.btnAddCardTransaction);
        btnExportCard = findViewById(R.id.btnExportCard);
        btnSendEmail = findViewById(R.id.btnSendEmail);
        
        tvCardTransactions = findViewById(R.id.tvCardTransactions);
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
        String amount = etAmount.getText().toString().trim();
        String customerName = etCustomerName.getText().toString().trim();
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
        if (selectedIssueType == R.id.rbCardSwallowed) {
            issueType = "CARD SWALLOWED";
        } else if (selectedIssueType == R.id.rbTransactionFailed) {
            issueType = "TRANSACTION FAILED";
        } else if (selectedIssueType == R.id.rbAmountNotDispensed) {
            issueType = "AMOUNT NOT DISPENSED";
        } else if (selectedIssueType == R.id.rbPinIssue) {
            issueType = "PIN ISSUE";
        } else if (selectedIssueType == R.id.rbOtherCardIssue) {
            issueType = "OTHER ISSUE";
        }

        // Validate required fields
        if (atmId.isEmpty() || cardNumber.isEmpty() || transactionDate.isEmpty() || 
            transactionTime.isEmpty() || transactionId.isEmpty() || amount.isEmpty() || 
            cardType.isEmpty() || issueType.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        CardTransaction transaction = new CardTransaction(atmId, cardNumber, transactionDate, 
                transactionTime, transactionId, amount, customerName, bankName, cardType, issueType);
        
        cardTransactionList.add(transaction);
        updateCardTransactionsView();
        clearForm();
        
        Toast.makeText(this, "Card transaction added successfully!", Toast.LENGTH_SHORT).show();
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
            sb.append("Transaction ").append(i + 1).append(":\n")
              .append("ATM ID: ").append(t.atmId).append("\n")
              .append("Card: ").append(t.cardType).append("\n")
              .append("Amount: ").append(t.amount).append("\n")
              .append("Issue: ").append(t.issueType).append("\n\n");
        }
        tvCardTransactions.setText(sb.toString());
    }
    
    private void clearForm() {
        etAtmId.setText("");
        etCardNumber.setText("");
        etTransactionDate.setText("");
        etTransactionTime.setText("");
        etTransactionId.setText("");
        etAmount.setText("");
        etCustomerName.setText("");
        etBankName.setText("");
        rgCardType.clearCheck();
        rgIssueType.clearCheck();
    }
    
    private void exportCardTransactions() {
        // For now, just show a message - you can implement CSV export later
        Toast.makeText(this, "Export feature will be implemented soon", Toast.LENGTH_SHORT).show();
    }
    
    private void sendCardTransactionEmail() {
        if (cardTransactionList.isEmpty()) {
            Toast.makeText(this, "No card transactions to email", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            
            String[] to = {"carddisputes@hitachi-payments.com", "support@hitachi-payments.com"};
            String[] cc = {"operations@hitachi-payments.com"};
            
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_CC, cc);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Card Transaction Dispute - " + getCurrentDate());
            emailIntent.putExtra(Intent.EXTRA_TEXT, createCardTransactionEmailContent());
            
            startActivity(Intent.createChooser(emailIntent, "Send Card Transaction Dispute..."));
            
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private String getCurrentDate() {
        return new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());
    }
    
    private String createCardTransactionEmailContent() {
        StringBuilder content = new StringBuilder();
        
        content.append("Dear Card Dispute Team,\n\n");
        content.append("I am reporting ").append(cardTransactionList.size())
               .append(" card transaction issue(s) at our ATM location.\n\n");
        
        content.append("SUMMARY:\n");
        int cardSwallowedCount = 0, transactionFailedCount = 0, amountNotDispensedCount = 0;
        double totalAmount = 0;
        
        for (CardTransaction t : cardTransactionList) {
            try {
                totalAmount += Double.parseDouble(t.amount);
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
            
            switch (t.issueType) {
                case "CARD SWALLOWED":
                    cardSwallowedCount++;
                    break;
                case "TRANSACTION FAILED":
                    transactionFailedCount++;
                    break;
                case "AMOUNT NOT DISPENSED":
                    amountNotDispensedCount++;
                    break;
            }
        }
        
        content.append("• Total Transactions: ").append(cardTransactionList.size()).append("\n");
        content.append("• Total Amount: ₹").append(totalAmount).append("\n");
        content.append("• Card Swallowed Cases: ").append(cardSwallowedCount).append("\n");
        content.append("• Transaction Failed Cases: ").append(transactionFailedCount).append("\n");
        content.append("• Amount Not Dispensed Cases: ").append(amountNotDispensedCount).append("\n\n");
        
        content.append("DETAILED TRANSACTIONS:\n");
        for (int i = 0; i < cardTransactionList.size(); i++) {
            CardTransaction t = cardTransactionList.get(i);
            content.append("\nTransaction ").append(i + 1).append(":\n");
            content.append("  • ATM ID: ").append(t.atmId).append("\n");
            content.append("  • Card Type: ").append(t.cardType).append("\n");
            content.append("  • Amount: ₹").append(t.amount).append("\n");
            content.append("  • Issue: ").append(t.issueType).append("\n");
            content.append("  • Bank: ").append(t.bankName).append("\n");
        }
        
        content.append("\nREQUEST:\n");
        content.append("Please investigate these card transaction issues and provide resolution at the earliest.\n\n");
        content.append("Best regards,\n");
        content.append("ATM Site Manager");
        
        return content.toString();
    }
    
    // CardTransaction inner class
    class CardTransaction {
        String atmId;
        String cardNumber;
        String transactionDate;
        String transactionTime;
        String transactionId;
        String amount;
        String customerName;
        String bankName;
        String cardType;
        String issueType;
        
        public CardTransaction(String atmId, String cardNumber, String transactionDate, 
                             String transactionTime, String transactionId, String amount,
                             String customerName, String bankName, String cardType, String issueType) {
            this.atmId = atmId;
            this.cardNumber = cardNumber;
            this.transactionDate = transactionDate;
            this.transactionTime = transactionTime;
            this.transactionId = transactionId;
            this.amount = amount;
            this.customerName = customerName;
            this.bankName = bankName;
            this.cardType = cardType;
            this.issueType = issueType;
        }
    }
}