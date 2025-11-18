package com.example.transactiondispute.cashmanagementapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transactiondispute.LoginActivity;
import com.example.transactiondispute.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CashmanagementActivity extends AppCompatActivity {
    
    private Button btnExportExcel;
    private static final int STORAGE_PERMISSION_CODE = 102;
    private File currentExportedFile;
    private RecyclerView recyclerView;
    private CashDataAdapter adapter;
    private List<CashData> originalDataList;
    private List<CashData> filteredDataList;
    private DataRepository dataRepository;
    private Button btngotoDataEntry;
    
    private Button btnSelectDate, btnClearFilter;
    private TextView tvSelectedDate, tvDataCount, tvTotalLoad, tvTotalIndent;
    private TextView tvTotal500, tvTotal200, tvTotal100;
    private EditText etSearch;
    
    private String selectedDate = "";
    private final Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    private static final int REQUEST_CODE_ADD_DATA = 1001;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String LAST_LOGIN_DATE = "last_login_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // ADD LOGIN VALIDATION HERE
        if (!isUserLoggedIn() || isNewDay()) {
            redirectToLogin();
            return;
        }
        
        setContentView(R.layout.activity_cashmanagement);
        
        dataRepository = new DataRepository(this);
        
        initializeViews();
        setupRecyclerView();
        setupEventListeners();
        loadData();
        
        setupSummaryNavigation();
        gotoDataEntry();
    }
    
    // ADD THESE LOGIN VALIDATION METHODS
    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean("isLoggedIn", false);
    }

    private boolean isNewDay() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String lastLoginDate = prefs.getString(LAST_LOGIN_DATE, "");
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return !lastLoginDate.equals(today);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(CashmanagementActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    // UPDATE THE EXISTING LOGOUT METHOD
    private void logout() {
        // Clear login state
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        // Go back to login activity
        Intent intent = new Intent(CashmanagementActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    // REST OF YOUR EXISTING CODE REMAINS THE SAME
    private void gotoDataEntry(){
        Button btngotoDataEntry = findViewById(R.id.btngotoDataEntry);
        btngotoDataEntry.setOnClickListener(v -> {
            Intent intent = new Intent(CashmanagementActivity.this, DataEntryActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_DATA);
        });
    }
    
    private void setupSummaryNavigation() {
        Button btnSummary = findViewById(R.id.btnSummary);
        btnSummary.setOnClickListener(v -> {
            Intent intent = new Intent(CashmanagementActivity.this, SummaryActivity.class);
            startActivity(intent);
        });
    }
    
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnClearFilter = findViewById(R.id.btnClearFilter);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvDataCount = findViewById(R.id.tvDataCount);
        etSearch = findViewById(R.id.etSearch);
        
        tvTotalLoad = findViewById(R.id.tvTotalLoad);
        tvTotalIndent = findViewById(R.id.tvTotalIndent);
        tvTotal500 = findViewById(R.id.tvTotal500);
        tvTotal200 = findViewById(R.id.tvTotal200);
        tvTotal100 = findViewById(R.id.tvTotal100);
        
        // Add this line for the export button
        btnExportExcel = findViewById(R.id.btnExportExcel);
    }
    
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CashDataAdapter(new ArrayList<>());
        
        adapter.setOnItemClickListener(new CashDataAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {}
            
            @Override
            public void onItemLongClick(int position) {
                CashData cashData = filteredDataList.get(position);
                showEditOptions(cashData);
            }
        });
        
        recyclerView.setAdapter(adapter);
    }
    
    private void showEditOptions(CashData cashData) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Options for " + cashData.getDate());
        builder.setItems(new String[]{"Edit", "Delete", "Cancel"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    editCashData(cashData);
                    break;
                case 1:
                    deleteCashData(cashData);
                    break;
            }
        });
        builder.show();
    }
    
    private void editCashData(CashData cashData) {
        Intent intent = new Intent(this, DataEntryActivity.class);
        intent.putExtra("edit_date", cashData.getDate());
        startActivityForResult(intent, REQUEST_CODE_ADD_DATA);
    }
    
    private void deleteCashData(CashData cashData) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete data for " + cashData.getDate() + "?")
            .setPositiveButton("Delete", (dialog, which) -> {
                dataRepository.deleteCashData(cashData.getDate());
                loadData();
                Toast.makeText(this, "Data deleted successfully!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void setupEventListeners() {
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnClearFilter.setOnClickListener(v -> clearFilters());
        
        // Add export button listener
        btnExportExcel.setOnClickListener(v -> exportToExcel());
        
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_ADD_DATA && resultCode == RESULT_OK) {
            loadData();
            Toast.makeText(this, "Data updated successfully!", Toast.LENGTH_SHORT).show();
        }
        
        // Handle storage permission result
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    performExport();
                } else {
                    Toast.makeText(this, "Storage permission denied. Cannot export file.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    
    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                
                selectedDate = dateFormat.format(calendar.getTime());
                tvSelectedDate.setText("Selected: " + selectedDate);
                filterData();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }
    
    private void clearFilters() {
        selectedDate = "";
        etSearch.setText("");
        tvSelectedDate.setText("All Dates");
        filterData();
    }
    
    private void loadData() {
        originalDataList = dataRepository.getCashData();
        
        if (originalDataList.isEmpty()) {
            originalDataList = dataRepository.getSampleData();
        }
        
        filteredDataList = new ArrayList<>(originalDataList);
        adapter.updateData(filteredDataList);
        updateSummary();
        updateDataCount();
    }
    
    private void filterData() {
        filteredDataList = new ArrayList<>();
        String searchQuery = etSearch.getText().toString().toLowerCase();
        
        for (CashData data : originalDataList) {
            boolean dateMatch = selectedDate.isEmpty() || 
                               data.getDate().contains(selectedDate);
            
            boolean searchMatch = searchQuery.isEmpty() ||
                                data.getDate().toLowerCase().contains(searchQuery) ||
                                String.valueOf(data.getIndentAmount()).contains(searchQuery) ||
                                String.valueOf(data.getSumLoadAmount()).contains(searchQuery);
            
            if (dateMatch && searchMatch) {
                filteredDataList.add(data);
            }
        }
        
        adapter.updateData(filteredDataList);
        updateSummary();
        updateDataCount();
    }
    
    private void updateDataCount() {
        String countText = "Showing " + filteredDataList.size() + " of " + originalDataList.size() + " records";
        tvDataCount.setText(countText);
    }
    
    private void updateSummary() {
        int totalLoad = 0;
        int totalIndent = 0;
        int total500 = 0;
        int total200 = 0;
        int total100 = 0;
        
        for (CashData data : filteredDataList) {
            totalLoad += data.getSumLoadAmount();
            totalIndent += data.getIndentAmount();
            total500 += data.getSum500Notes();
            total200 += data.getSum200Notes();
            total100 += data.getSum100Notes();
        }
        
        tvTotalLoad.setText("Total Load: ₹" + totalLoad);
        tvTotalIndent.setText("Total Indent: ₹" + totalIndent);
        tvTotal500.setText("500 Notes: " + total500);
        tvTotal200.setText("200 Notes: " + total200);
        tvTotal100.setText("100 Notes: " + total100);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Optional: Add validation on resume as well
        if (!isUserLoggedIn() || isNewDay()) {
            redirectToLogin();
            return;
        }
        loadData();
    }
    
    // Export to Excel methods
    private void exportToExcel() {
        if (filteredDataList.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkStoragePermission()) {
            performExport();
        } else {
            requestStoragePermission();
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }

    private void performExport() {
        try {
            // Create CSV content
            StringBuilder csvContent = new StringBuilder();
            
            // Add header row
            csvContent.append("Date,Indent Amount,500 Notes,200 Notes,100 Notes,")
                      .append("Load Amount,EOD Received,EOD 500 Notes,EOD 200 Notes,EOD 100 Notes,")
                      .append("EOD Amount,Due EOD Amount,Loading Time\n");
            
            // Add data rows
            for (CashData data : filteredDataList) {
                csvContent.append(csvEscape(data.getDate())).append(",");
                csvContent.append(csvEscape(String.valueOf(data.getIndentAmount()))).append(",");
                csvContent.append(csvEscape(String.valueOf(data.getSum500Notes()))).append(",");
                csvContent.append(csvEscape(String.valueOf(data.getSum200Notes()))).append(",");
                csvContent.append(csvEscape(String.valueOf(data.getSum100Notes()))).append(",");
                csvContent.append(csvEscape(String.valueOf(data.getSumLoadAmount()))).append(",");
                csvContent.append(csvEscape(data.getEodReceivedFromPurge())).append(",");
                csvContent.append(csvEscape(String.valueOf(data.getEod500Notes()))).append(",");
                csvContent.append(csvEscape(String.valueOf(data.getEod200Notes()))).append(",");
                csvContent.append(csvEscape(String.valueOf(data.getEod100Notes()))).append(",");
                csvContent.append(csvEscape(String.valueOf(data.getSumEodAmount()))).append(",");
                csvContent.append(csvEscape(String.valueOf(data.getDueEodAmount()))).append(",");
                csvContent.append(csvEscape(data.getLoadingTime())).append("\n");
            }
            
            // Generate filename with timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "Cash_Management_Data_" + timeStamp + ".csv";
            
            // Save to Downloads folder
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);
            
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(csvContent.toString().getBytes());
            outputStream.close();
            
            currentExportedFile = file;
            
            // Show success message
            showExportSuccessDialog(file.getAbsolutePath());
            
            Toast.makeText(this, "Data exported successfully to Downloads!", Toast.LENGTH_LONG).show();
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

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
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Export Successful")
            .setMessage("Cash management data has been exported to:\n" + filePath)
            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
            .show();
    }

    // Handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performExport();
            } else {
                Toast.makeText(this, "Storage permission denied. Cannot export file.", Toast.LENGTH_LONG).show();
            }
        }
    }
}