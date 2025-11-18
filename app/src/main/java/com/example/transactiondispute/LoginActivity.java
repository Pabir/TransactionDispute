package com.example.transactiondispute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.transactiondispute.R;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.transactiondispute.cashmanagementapp.CashmanagementActivity;
import com.example.transactiondispute.notecounter.NoteCounterActivity;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText etPasscode;
    private Button btnNoteCounter;
    private Button btnLogin;
    private TextView tvError;
    private Button btnReport;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String LAST_LOGIN_DATE = "last_login_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        
        // Check if user is already logged in (same day)
        if (isUserLoggedIn() && !isNewDay()) {
            startMainActivity();
            return;
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePasscode();
            }
        });

        // Add in your MainActivity
        Button btnNoteCounter = findViewById(R.id.btnNoteCounter); // You'll need to add this in XML
        btnNoteCounter.setOnClickListener(
                v -> {
                    Intent intent = new Intent(LoginActivity.this, NoteCounterActivity.class);
                    startActivity(intent);
                });
               Button btnReport = findViewById(R.id.btnReport); // You'll need to add this in XML
        btnReport.setOnClickListener(
                v -> {
                    Intent intent = new Intent(LoginActivity.this, CashmanagementActivity.class);
                    startActivity(intent);
                });
    }

    private void initializeViews() {
        etPasscode = findViewById(R.id.etPasscode);
        btnLogin = findViewById(R.id.btnLogin);
        tvError = findViewById(R.id.tvError);
    }

    private void validatePasscode() {
        String enteredPasscode = etPasscode.getText().toString().trim();
        
        if (enteredPasscode.isEmpty()) {
            showError("Please enter passcode");
            return;
        }

        // Get today's dynamic passcode using full MD5 hash (30 characters)
        String todayPasscode = generateDailyPasscode();
        
        if (enteredPasscode.equals(todayPasscode)) {
            // Save login state and date
            saveLoginState();
            startMainActivity();
        } else {
            showError("Invalid passcode. Please try again.");
            etPasscode.setText("");
        }
    }

    private String generateDailyPasscode() {
        try {
            // Get current date in required format: ddMMyyyy
            String currentDate = new SimpleDateFormat("ddMMyyyy", Locale.getDefault()).format(new Date());
            
            // Create the base string: currentDate + Pabirul + 2025
            String baseString = currentDate + "Pabirul" + "2025";
            
            // Generate MD5 hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(baseString.getBytes());
            byte[] messageDigest = digest.digest();
            
            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            // Take first 30 characters for the passcode (MD5 is 32 chars, we take 30)
            return hexString.toString().substring(0, 30);
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // Fallback method if MD5 fails
            return generateFallbackPasscode();
        }
    }

    private String generateFallbackPasscode() {
        // Simple fallback method if MD5 fails
        String currentDate = new SimpleDateFormat("ddMMyyyy", Locale.getDefault()).format(new Date());
        String baseString = currentDate + "Pabirul" + "2025";
        // Create a longer fallback hash
        String hash = String.valueOf(Math.abs(baseString.hashCode()));
        // Pad or truncate to 30 characters
        while (hash.length() < 30) {
            hash += hash; // Repeat if too short
        }
        return hash.substring(0, 30);
    }

    private void saveLoginState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", true);
        
        // Save today's date to check for new day
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        editor.putString(LAST_LOGIN_DATE, today);
        
        editor.apply();
    }

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

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, LandingActivity.class);
        startActivity(intent);
        finish(); // Close login activity
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    // Method to get today's passcode (for you to share with users)
    public String getTodaysPasscode() {
        return generateDailyPasscode();
    }

    // Temporary method to display today's passcode (remove after testing)
    private void showTodaysPasscode() {
        String todayPasscode = generateDailyPasscode();
        String currentDate = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());
        
        Toast.makeText(this, 
            "Today's Passcode (30 chars): " + todayPasscode, 
            Toast.LENGTH_LONG).show();
        
        // Also log to console for easy copying
        System.out.println("Date: " + currentDate);
        System.out.println("Passcode: " + todayPasscode);
        System.out.println("Passcode Length: " + todayPasscode.length());
    }
}