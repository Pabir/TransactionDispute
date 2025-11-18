package com.example.transactiondispute.cashmanagementapp;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import java.util.Date;
import android.content.Intent;
import com.example.transactiondispute.LoginActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.transactiondispute.R;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DataEntryActivity extends AppCompatActivity {
    
    private Button btnEntryDate, btnSave, btnCancel;
    private TextView tvSelectedEntryDate, tvLoadAmount, tvEodAmount, tvDueEodAmount;
    private EditText etIndentAmount, et500Notes, et200Notes, et100Notes;
    private EditText etEodReceived, etEod500, etEod200, etEod100, etLoadingTime;
    
    private final Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String selectedDate = "";
    
    private DataRepository dataRepository;
    private CashData existingData; // For editing existing data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         // ADD THIS VALIDATION
    if (!isUserLoggedIn() || isNewDay()) {
        redirectToLogin();
        return;
    }
        setContentView(R.layout.activity_data_entry);
        
        dataRepository = new DataRepository(this);
        
        initializeViews();
        setupEventListeners();
        setupTextWatchers();
        
        // Check if we're editing existing data
        checkForEditMode();
    }
    // ADD THESE METHODS TO EACH ACTIVITY
private boolean isUserLoggedIn() {
    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
    return prefs.getBoolean("isLoggedIn", false);
}

private boolean isNewDay() {
    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
    String lastLoginDate = prefs.getString("last_login_date", "");
    String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
    return !lastLoginDate.equals(today);
}

private void redirectToLogin() {
    Intent intent = new Intent(this, LoginActivity.class);
    startActivity(intent);
    finish();
}
    
    private void checkForEditMode() {
        String editDate = getIntent().getStringExtra("edit_date");
        if (editDate != null) {
            // Load existing data for editing
            existingData = dataRepository.getCashDataByDate(editDate);
            if (existingData != null) {
                populateForm(existingData);
                setTitle("Edit Cash Data");
            }
        }
    }
    
    private void populateForm(CashData data) {
        selectedDate = data.getDate();
        tvSelectedEntryDate.setText("Editing: " + selectedDate);
        
        etIndentAmount.setText(String.valueOf(data.getIndentAmount()));
        et500Notes.setText(String.valueOf(data.getSum500Notes()));
        et200Notes.setText(String.valueOf(data.getSum200Notes()));
        et100Notes.setText(String.valueOf(data.getSum100Notes()));
        etEodReceived.setText(data.getEodReceivedFromPurge());
        etEod500.setText(String.valueOf(data.getEod500Notes()));
        etEod200.setText(String.valueOf(data.getEod200Notes()));
        etEod100.setText(String.valueOf(data.getEod100Notes()));
        etLoadingTime.setText(data.getLoadingTime());
        
        // Trigger calculations
        calculateAmounts();
    }
    
    private void initializeViews() {
        btnEntryDate = findViewById(R.id.btnEntryDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        
        tvSelectedEntryDate = findViewById(R.id.tvSelectedEntryDate);
        tvLoadAmount = findViewById(R.id.tvLoadAmount);
        tvEodAmount = findViewById(R.id.tvEodAmount);
        tvDueEodAmount = findViewById(R.id.tvDueEodAmount);
        
        etIndentAmount = findViewById(R.id.etIndentAmount);
        et500Notes = findViewById(R.id.et500Notes);
        et200Notes = findViewById(R.id.et200Notes);
        et100Notes = findViewById(R.id.et100Notes);
        etEodReceived = findViewById(R.id.etEodReceived);
        etEod500 = findViewById(R.id.etEod500);
        etEod200 = findViewById(R.id.etEod200);
        etEod100 = findViewById(R.id.etEod100);
        etLoadingTime = findViewById(R.id.etLoadingTime);
    }
    
    private void setupEventListeners() {
        // Date picker
        btnEntryDate.setOnClickListener(v -> showDatePicker());
        
        // Save button
        btnSave.setOnClickListener(v -> saveData());
        
        // Cancel button
        btnCancel.setOnClickListener(v -> finish());
    }
    
    private void setupTextWatchers() {
        // Add text watchers for real-time calculations
        TextWatcher calculationWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                calculateAmounts();
            }
        };
        
        et500Notes.addTextChangedListener(calculationWatcher);
        et200Notes.addTextChangedListener(calculationWatcher);
        et100Notes.addTextChangedListener(calculationWatcher);
        etEod500.addTextChangedListener(calculationWatcher);
        etEod200.addTextChangedListener(calculationWatcher);
        etEod100.addTextChangedListener(calculationWatcher);
        etEodReceived.addTextChangedListener(calculationWatcher);
    }
    
    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                
                selectedDate = dateFormat.format(calendar.getTime());
                tvSelectedEntryDate.setText("Selected: " + selectedDate);
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }
    
    private void calculateAmounts() {
        try {
            // Calculate Load Amount (Excel: =B2*500+C2*200+D2*100)
            int notes500 = getIntValue(et500Notes);
            int notes200 = getIntValue(et200Notes);
            int notes100 = getIntValue(et100Notes);
            int loadAmount = (notes500 * 500) + (notes200 * 200) + (notes100 * 100);
            tvLoadAmount.setText("Load Amount: ₹" + loadAmount);
            
            // Calculate EOD Amount (Excel: =G2*500+H2*200+I2*100)
            int eod500 = getIntValue(etEod500);
            int eod200 = getIntValue(etEod200);
            int eod100 = getIntValue(etEod100);
            int eodAmount = (eod500 * 500) + (eod200 * 200) + (eod100 * 100);
            tvEodAmount.setText("EOD Amount: ₹" + eodAmount);
            
            // Calculate Due EOD Amount (Excel: =F2-J2)
            int eodReceived = getIntValue(etEodReceived);
            int dueEodAmount = eodReceived - eodAmount;
            tvDueEodAmount.setText("Due EOD Amount: ₹" + dueEodAmount);
            
            // Color code due amount
            if (dueEodAmount > 0) {
                tvDueEodAmount.setBackgroundColor(0xFFFFEBEE); // Light red
            } else if (dueEodAmount < 0) {
                tvDueEodAmount.setBackgroundColor(0xFFFFF8E1); // Light yellow
            } else {
                tvDueEodAmount.setBackgroundColor(0xFFE8F5E8); // Light green
            }
            
        } catch (Exception e) {
            // Handle calculation errors
            tvLoadAmount.setText("Load Amount: ₹0");
            tvEodAmount.setText("EOD Amount: ₹0");
            tvDueEodAmount.setText("Due EOD Amount: ₹0");
        }
    }
    
    private int getIntValue(EditText editText) {
        try {
            String text = editText.getText().toString().trim();
            return text.isEmpty() ? 0 : Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private void saveData() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Get all values
            int indentAmount = getIntValue(etIndentAmount);
            int notes500 = getIntValue(et500Notes);
            int notes200 = getIntValue(et200Notes);
            int notes100 = getIntValue(et100Notes);
            String eodReceived = etEodReceived.getText().toString().trim();
            int eod500 = getIntValue(etEod500);
            int eod200 = getIntValue(etEod200);
            int eod100 = getIntValue(etEod100);
            String loadingTime = etLoadingTime.getText().toString().trim();
            
            // Use empty string instead of "0" for EOD Received if not provided
            if (eodReceived.isEmpty()) {
                eodReceived = "0";
            }
            
            // Create CashData object
            CashData cashData = new CashData(
                selectedDate,
                indentAmount,
                notes500,
                notes200,
                notes100,
                eodReceived,
                eod500,
                eod200,
                eod100,
                loadingTime
            );
            
            // Save to database
            dataRepository.saveCashData(cashData);
            
            // Set result for MainActivity to refresh
            setResult(RESULT_OK);
            
            Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}