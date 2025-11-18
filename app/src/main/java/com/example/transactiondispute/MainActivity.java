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
import com.example.transactiondispute.R;
import com.example.transactiondispute.cashmanagementapp.CashmanagementActivity;
import com.google.android.material.textfield.TextInputEditText;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Transaction class defined in the same file
class Transaction {
    private String atmId;
    private String transDate;
    private String time;
    private String transactionId;
    private String utr;
    private String customerName;
    private String disputeAmount;
    private String transactionType;

    public Transaction() {}

    public Transaction(String atmId, String transDate, String time, String transactionId, 
                      String utr, String customerName, String disputeAmount, String transactionType) {
        this.atmId = atmId;
        this.transDate = transDate;
        this.time = time;
        this.transactionId = transactionId;
        this.utr = utr;
        this.customerName = customerName;
        this.disputeAmount = disputeAmount;
        this.transactionType = transactionType;
    }

    // Getters and Setters
    public String getAtmId() { return atmId; }
    public void setAtmId(String atmId) { this.atmId = atmId; }

    public String getTransDate() { return transDate; }
    public void setTransDate(String transDate) { this.transDate = transDate; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getUtr() { return utr; }
    public void setUtr(String utr) { this.utr = utr; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getDisputeAmount() { return disputeAmount; }
    public void setDisputeAmount(String disputeAmount) { this.disputeAmount = disputeAmount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
}

public class MainActivity extends AppCompatActivity {

    private Button btnLogout;
    private Button btnGoToCashManagement;
    
    private Button btnSendEmail; // Add this with other button declarations
    private TextInputEditText etAtmId, etTransDate, etTime, etTransactionId, etUtr, etCustomerName, etDisputeAmount;
    private RadioGroup rgTransactionType;
    private Button btnAddTransaction, btnExport, btnShareWhatsApp;
    private TextView tvTransactions;
    
    private List<Transaction> transactionList;
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int MANAGE_STORAGE_PERMISSION_CODE = 101;
    private File currentExportedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        transactionList = new ArrayList<>();
        
        btnAddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTransaction();
            }
        });
        
       btnGoToCashManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCashManagement();
            }
        });
        
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndExport();
            }
        });

        btnShareWhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToWhatsApp();
            }
        });
        
        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEnhancedEmail();
            }
        });
        
         // Add logout listener
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        
        
        
        
    }
   @Override
public void onBackPressed() {
    // Instead of closing the app, go back to MainActivity
    Intent intent = new Intent(MainActivity.this, CashmanagementActivity.class);
    startActivity(intent);
    finish();
}
    
    
     private void logout() {
        // Clear login state
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        // Go back to login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void initializeViews() {
        etAtmId = findViewById(R.id.etAtmId);
        etTransDate = findViewById(R.id.etTransDate);
        etTime = findViewById(R.id.etTime);
        etTransactionId = findViewById(R.id.etTransactionId);
        etUtr = findViewById(R.id.etUtr);
        etCustomerName = findViewById(R.id.etCustomerName);
        etDisputeAmount = findViewById(R.id.etDisputeAmount);
        rgTransactionType = findViewById(R.id.rgTransactionType);
        btnAddTransaction = findViewById(R.id.btnAddTransaction);
        btnExport = findViewById(R.id.btnExport);
        btnShareWhatsApp = findViewById(R.id.btnShareWhatsApp);
        tvTransactions = findViewById(R.id.tvTransactions);
        btnSendEmail = findViewById(R.id.btnSendEmail);
        
        btnLogout = findViewById(R.id.btnLogout);
        btnGoToCashManagement = findViewById(R.id.btnGoToCashManagement);
        // Initially disable WhatsApp button
        btnShareWhatsApp.setEnabled(false);
        btnSendEmail.setEnabled(false);

    }
    
    private void goToCashManagement() {
        Intent intent = new Intent(MainActivity.this, CashmanagementActivity.class);
        startActivity(intent);
    }

    private void addTransaction() {
        String atmId = etAtmId.getText().toString().trim();
        String transDate = etTransDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String transactionId = etTransactionId.getText().toString().trim();
        String utr = etUtr.getText().toString().trim();
        String customerName = etCustomerName.getText().toString().trim();
        String disputeAmount = etDisputeAmount.getText().toString().trim();
        
        String transactionType = "";
        int selectedId = rgTransactionType.getCheckedRadioButtonId();
        if (selectedId == R.id.rbWithdrawal) {
            transactionType = "WITHDRAWAL";
        } else if (selectedId == R.id.rbDeposit) {
            transactionType = "DEPOSIT";
        }

        // Validate required fields
        if (atmId.isEmpty() || transDate.isEmpty() || time.isEmpty() || 
            transactionId.isEmpty() || utr.isEmpty() || disputeAmount.isEmpty() || 
            transactionType.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Transaction transaction = new Transaction(atmId, transDate, time, transactionId, 
                utr, customerName, disputeAmount, transactionType);
        
        transactionList.add(transaction);
        updateTransactionsView();
        clearForm();
        
        Toast.makeText(this, "Transaction added successfully!", Toast.LENGTH_SHORT).show();
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
            sb.append("Transaction ").append(i + 1).append(":\n")
              .append("ATM ID: ").append(t.getAtmId()).append("\n")
              .append("Amount: ").append(t.getDisputeAmount()).append("\n")
              .append("Type: ").append(t.getTransactionType()).append("\n\n");
        }
        tvTransactions.setText(sb.toString());
        btnShareWhatsApp.setEnabled(true);
        btnSendEmail.setEnabled(true);
    }

    private void clearForm() {
        etAtmId.setText("");
        etTransDate.setText("");
        etTime.setText("");
        etTransactionId.setText("");
        etUtr.setText("");
        etCustomerName.setText("");
        etDisputeAmount.setText("");
        rgTransactionType.clearCheck();
    }

    private void checkPermissionAndExport() {
        if (transactionList.isEmpty()) {
            Toast.makeText(this, "No transactions to export", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check current permission status
        if (isStoragePermissionGranted()) {
            // Permission already granted, export the file
            exportToExcel();
        } else {
            // Permission not granted, request it
            requestStoragePermission();
        }
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+) - Check MANAGE_EXTERNAL_STORAGE permission
            return Environment.isExternalStorageManager();
        } else {
            // Android 10 and below (API 29 and below) - Check WRITE_EXTERNAL_STORAGE permission
            return ContextCompat.checkSelfPermission(
                            this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

   private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ - Show explanation dialog first, then request permission
            showAndroid11PermissionExplanation();
        } else {
            // Android 10 and below - Directly request WRITE_EXTERNAL_STORAGE
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }
    private void showAndroid11PermissionExplanation() {
    new AlertDialog.Builder(this)
            .setTitle("Storage Access Required")
            .setMessage("To save Excel files to your Downloads folder, this app needs permission to manage all files on your device. This allows you to easily access and share the exported files.")
            .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Open settings for MANAGE_EXTERNAL_STORAGE permission
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
                        startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE);
                    } catch (Exception e) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE);
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, export the file
                Toast.makeText(this, "Storage permission granted!", Toast.LENGTH_SHORT).show();
                exportToExcel();
            } else {
                // Permission denied
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // User denied, but not permanently - show explanation
                    showPermissionExplanationDialog();
                } else {
                    // User denied permanently - guide to settings
                    showPermissionPermanentlyDeniedDialog();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MANAGE_STORAGE_PERMISSION_CODE) {
            // Check if permission was granted after returning from settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Permission granted
                    Toast.makeText(this, "Storage permission granted!", Toast.LENGTH_SHORT).show();
                    exportToExcel();
                } else {
                    // Permission denied
                    Toast.makeText(
                                    this,
                                    "Storage permission denied. Cannot export file.",
                                    Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
    }

    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Storage Permission Required")
                .setMessage("This app needs storage permission to save Excel files to your device. The files will be saved in your Downloads folder where you can easily access them.")
                .setPositiveButton("Grant Permission", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestStoragePermission();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPermissionPermanentlyDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Permanently Denied")
                .setMessage("You have permanently denied storage permission. Please go to app settings and grant the permission to export files.")
                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openAppSettings();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void exportToExcel() {
        try {
            // Create CSV content instead of Excel to avoid POI issues
            StringBuilder csvContent = new StringBuilder();

            // Add header row
            csvContent.append(
                    "ATM ID,Trans Date,Time,Transaction ID,UTR,Customer Name,Dispute Amount,WITHDRAWAL/DEPOSIT\n");

            // Add data rows
            for (Transaction transaction : transactionList) {
                csvContent.append(csvEscape(transaction.getAtmId())).append(",");
                csvContent.append(csvEscape(transaction.getTransDate())).append(",");
                csvContent.append(csvEscape(transaction.getTime())).append(",");
                csvContent.append(csvEscape(transaction.getTransactionId())).append(",");
                csvContent.append(csvEscape(transaction.getUtr())).append(",");
                csvContent.append(csvEscape(transaction.getCustomerName())).append(",");
                csvContent.append(csvEscape(transaction.getDisputeAmount())).append(",");
                csvContent.append(csvEscape(transaction.getTransactionType())).append("\n");
            }

            // Save the file as CSV
            String timeStamp =
                    new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "Transaction_Disputes_" + timeStamp + ".csv";

            File downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(csvContent.toString().getBytes());
            outputStream.close();

            currentExportedFile = file;
            showExportSuccessDialog(file.getAbsolutePath());

            Toast.makeText(this, "File exported successfully to Downloads!", Toast.LENGTH_LONG)
                    .show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting file: " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    // Helper method to escape CSV fields
    private String csvEscape(String field) {
        if (field == null || field.isEmpty()) {
            return "";
        }

        // If field contains comma, newline, or quotes, wrap it in quotes and escape existing quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }

        return field;
    }

    private void showExportSuccessDialog(String filePath) {
        new AlertDialog.Builder(this)
                .setTitle("Export Successful")
                .setMessage("Excel file has been exported to:\n" + filePath + "\n\nDo you want to share it via WhatsApp?")
                .setPositiveButton("Share to WhatsApp", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shareToWhatsApp();
                    }
                })
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void shareToWhatsApp() {
        if (currentExportedFile == null || !currentExportedFile.exists()) {
            Toast.makeText(this, "Please export transactions first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create content URI using FileProvider
            Uri fileUri =
                    FileProvider.getUriForFile(
                            this,
                            getApplicationContext().getPackageName() + ".provider",
                            currentExportedFile);

            // Grant temporary read permission to the content URI
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setType(getMimeType(currentExportedFile));
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

            // Add email content suggestion
            String emailContent = createEmailContent();
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Transaction Dispute File\n\n" + emailContent);

            // Try WhatsApp specifically
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            whatsappIntent.setType(getMimeType(currentExportedFile));
            whatsappIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            whatsappIntent.putExtra(
                    Intent.EXTRA_TEXT, "Transaction Dispute File\n\n" + emailContent);

            try {
                startActivity(whatsappIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                // WhatsApp not installed, use general share
                startActivity(Intent.createChooser(shareIntent, "Share Transaction File"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error sharing file: " + e.getMessage(), Toast.LENGTH_LONG).show();

            // Fallback: Share just the text content
            shareTextContentOnly();
        }
    }

    // Helper method to get MIME type
    private String getMimeType(File file) {
        String fileName = file.getName();
        if (fileName.endsWith(".csv")) {
            return "text/csv";
        } else if (fileName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else {
            return "*/*";
        }
    }

    // Fallback method to share just the text content
    private void shareTextContentOnly() {
        StringBuilder textContent = new StringBuilder();
        textContent.append("Transaction Dispute Details:\n\n");

        // Add all transaction details as text
        for (int i = 0; i < transactionList.size(); i++) {
            Transaction t = transactionList.get(i);
            textContent.append("Transaction ").append(i + 1).append(":\n");
            textContent.append("ATM ID: ").append(t.getAtmId()).append("\n");
            textContent.append("Date: ").append(t.getTransDate()).append("\n");
            textContent.append("Time: ").append(t.getTime()).append("\n");
            textContent.append("Transaction ID: ").append(t.getTransactionId()).append("\n");
            textContent.append("UTR: ").append(t.getUtr()).append("\n");
            textContent.append("Customer: ").append(t.getCustomerName()).append("\n");
            textContent.append("Amount: ").append(t.getDisputeAmount()).append("\n");
            textContent.append("Type: ").append(t.getTransactionType()).append("\n\n");
        }

        Intent textShareIntent = new Intent(Intent.ACTION_SEND);
        textShareIntent.setType("text/plain");
        textShareIntent.putExtra(Intent.EXTRA_TEXT, textContent.toString());
        startActivity(Intent.createChooser(textShareIntent, "Share Transaction Details"));
    }

    private String createEmailContent() {
        StringBuilder content = new StringBuilder();
        content.append("Dear Sir/Madam,\n\n");
        content.append("I am writing to report unsuccessful UPI withdrawal and deposit transactions that occurred at your ATM. Despite the transactions failing, the amounts were debited from / not credited to the customers' bank accounts.\n\n");
        content.append("Please find the attached Excel file containing the transaction details for your investigation.\n\n");
        
        double totalAmount = 0;
        for (Transaction t : transactionList) {
            try {
                totalAmount += Double.parseDouble(t.getDisputeAmount());
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }
        
        
        return content.toString();
    }

    private void sendEnhancedEmail() {
        if (currentExportedFile == null || !currentExportedFile.exists()) {
            Toast.makeText(this, "Please export transactions first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri fileUri =
                    FileProvider.getUriForFile(
                            this,
                            getApplicationContext().getPackageName() + ".provider",
                            currentExportedFile);

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");

            // Multiple recipients
            String[] to = {
                "WLARecon@hitachi-payments.com",
                "AppSupport@hitachi-payments.com",
                "WLASupport@hitachi-payments.com",
                "prajakta.madage@hitachi-payments.com",
                "kalpana.s@hitachi-payments.com",
                "Sandeep.Gadekar@hitachi-payments.com",
                "abdul.Shaikh@hitachi-payments.com",
                "Hanumant.Sable@hitachi-payments.com"
                
            };
            String[] cc = {
                "mf.teacheasyservices.com",
                "dibyendu.majumder@hitachi-payments.com",
                "jagdish.panchal@hitachi-payments.com",
            };

            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_CC, cc);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getEmailSubject());
            emailIntent.putExtra(Intent.EXTRA_TEXT, createDetailedEmailContent());
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(emailIntent, "Send dispute email..."));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot send email: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getEmailSubject() {
        return "Transaction Dispute - "
                + transactionList.size()
                + " Failed UPI Transactions - "
                + new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());
    }

    private String createDetailedEmailContent() {
    StringBuilder content = new StringBuilder();

    content.append("Dear Hitachi Support Team,\n\n");

    content.append("I am writing to report ")
            .append(transactionList.size())
            .append(" unsuccessful UPI withdrawal and deposit transactions that occurred at your ATM. ")
            .append("Despite the transactions failing, the amounts were debited from our customers' accounts.\n\n");

    content.append("TRANSACTION SUMMARY:\n");

    double totalAmount = 0;
    int withdrawalCount = 0;
    int depositCount = 0;

    for (Transaction t : transactionList) {
        try {
            totalAmount += Double.parseDouble(t.getDisputeAmount());
        } catch (NumberFormatException e) {
            // Ignore parsing errors
        }
        if ("WITHDRAWAL".equals(t.getTransactionType())) {
            withdrawalCount++;
        } else {
            depositCount++;
        }
    }

    // Add transaction details as bullet points
    content.append("TRANSACTION DETAILS:\n");
    for (int i = 0; i < transactionList.size(); i++) {
        Transaction t = transactionList.get(i);
        content.append("\nTransaction ").append(i + 1).append(":\n");
        content.append("  • ATM ID: ").append(t.getAtmId()).append("\n");
        content.append("  • Date: ").append(t.getTransDate()).append("\n");
        content.append("  • Time: ").append(t.getTime()).append("\n");
        content.append("  • Transaction ID: ").append(t.getTransactionId()).append("\n");
        content.append("  • UTR: ").append(t.getUtr()).append("\n");
        content.append("  • Customer Name: ").append(t.getCustomerName()).append("\n");
        content.append("  • Amount: ₹").append(t.getDisputeAmount()).append("\n");
        content.append("  • Type: ").append(t.getTransactionType()).append("\n");
    }
    content.append("\n");

    content.append("The detailed transaction list is also attached in the CSV file for your reference.\n\n");

    content.append("Thank you for your prompt attention to this matter.\n\n");

    return content.toString();
}
}