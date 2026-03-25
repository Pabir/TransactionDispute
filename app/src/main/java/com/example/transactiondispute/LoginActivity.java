package com.example.transactiondispute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.transactiondispute.api.SupabaseService;
import com.example.transactiondispute.cashmanagementapp.CashmanagementActivity;
import com.example.transactiondispute.models.AuthModels;
import com.example.transactiondispute.notecounter.NoteCounterActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etFranchiseeId, etPassword, etPasscode;
    private Button btnLogin;
    private TextView tvError;
    private ProgressBar progressBar;
    private SupabaseService supabaseService;
    
    private static final String PREFS_NAME = "AppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initRetrofit();
        initializeViews();
        
        if (isUserLoggedIn()) {
            startLandingActivity();
            return;
        }

        btnLogin.setOnClickListener(v -> performLogin());

        findViewById(R.id.btnNoteCounter).setOnClickListener(v -> {
            startActivity(new Intent(this, NoteCounterActivity.class));
        });
        
        findViewById(R.id.btnReport).setOnClickListener(v -> {
            startActivity(new Intent(this, CashmanagementActivity.class));
        });
    }

    private void initRetrofit() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
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
        etFranchiseeId = findViewById(R.id.etFranchiseeId);
        etPassword = findViewById(R.id.etPassword);
        etPasscode = findViewById(R.id.etPasscode);
        btnLogin = findViewById(R.id.btnLogin);
        tvError = findViewById(R.id.tvError);
        progressBar = findViewById(R.id.loginProgress);
    }

    private void performLogin() {
        String inputId = etFranchiseeId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passcode = etPasscode.getText().toString().trim();

        if (inputId.isEmpty() || password.isEmpty() || passcode.isEmpty()) {
            showError("Please enter ID, Password and Passcode");
            return;
        }

        // Validate MD5 Passcode (Dynamic based on current date)
        if (!validatePasscode(passcode)) {
            showError("Invalid MD5 Passcode");
            return;
        }

        String email = inputId.contains("@") ? inputId : 
                      (inputId.equalsIgnoreCase("admin") ? "admin@yourdomain.com" : inputId + "@franchisee.com");

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        tvError.setVisibility(View.GONE);

        supabaseService.login(new AuthModels.LoginRequest(email, password))
                .enqueue(new Callback<AuthModels.AuthResponse>() {
            @Override
            public void onResponse(Call<AuthModels.AuthResponse> call, Response<AuthModels.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fetchProfile(response.body());
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    showError("Login failed: Invalid ID or password");
                }
            }

            @Override
            public void onFailure(Call<AuthModels.AuthResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private boolean validatePasscode(String input) {
        if (input == null || input.isEmpty()) return false;

        // 1. Get current date in DDMMYYYY format
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        
        // 2. Construct the daily string
        String dailyString = currentDate + "Pabirul2026";
        
        // 3. Calculate expected MD5 hash
        String expectedHash = md5(dailyString);

        // 4. Compare input with the expected daily hash
        return input.equalsIgnoreCase(expectedHash);
    }

    private String md5(String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void fetchProfile(AuthModels.AuthResponse auth) {
        String authHeader = "Bearer " + auth.accessToken;
        String queryId = "eq." + auth.user.id;
        
        supabaseService.getProfile(authHeader, "0-0", queryId).enqueue(new Callback<List<AuthModels.Profile>>() {
            @Override
            public void onResponse(Call<List<AuthModels.Profile>> call, Response<List<AuthModels.Profile>> response) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    AuthModels.Profile profile = response.body().get(0);
                    saveUserSession(auth.accessToken, profile);
                    startLandingActivity();
                } else {
                    showError("Profile record not found in database.");
                }
            }

            @Override
            public void onFailure(Call<List<AuthModels.Profile>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                showError("Failed to fetch user profile data.");
            }
        });
    }

    private void saveUserSession(String token, AuthModels.Profile profile) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("access_token", token);
        editor.putString("user_id", profile.id);
        editor.putString("franchisee_id", profile.franchiseeId);
        editor.putString("user_role", profile.role);
        editor.apply();
    }

    private boolean isUserLoggedIn() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean("isLoggedIn", false);
    }

    private void startLandingActivity() {
        startActivity(new Intent(this, LandingActivity.class));
        finish();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
