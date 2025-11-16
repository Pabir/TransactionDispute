package com.example.transactiondispute.notecounter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.transactiondispute.R;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.text.NumberFormat;
import java.util.Locale;

public class NoteCounterActivity extends AppCompatActivity {

    private int total500 = 0;
    private int total200 = 0;
    private int total100 = 0;
    
    private EditText etNew500, etNew200, etNew100;
    private TextView tvTotal500, tvTotal200, tvTotal100, tvGrandTotal;
   private TextInputEditText et500Before, et500After;
    private TextInputEditText et200Before, et200After;
    private TextInputEditText et100Before, et100After;
    private TextView tv500Used, tv200Used, tv100Used, tvTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notecounter);

        etNew500 = findViewById(R.id.etNew500);
        etNew200 = findViewById(R.id.etNew200);
        etNew100 = findViewById(R.id.etNew100);
        tvTotal500 = findViewById(R.id.tvTotal500);
        tvTotal200 = findViewById(R.id.tvTotal200);
        tvTotal100 = findViewById(R.id.tvTotal100);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);

        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnReset = findViewById(R.id.btnReset);
        btnAdd.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addToTotals();
                    }
                });

        btnReset.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resetTotals();
                    }
                });
        // Initialize views
        et500Before = findViewById(R.id.et500Before);
        et500After = findViewById(R.id.et500After);
        et200Before = findViewById(R.id.et200Before);
        et200After = findViewById(R.id.et200After);
        et100Before = findViewById(R.id.et100Before);
        et100After = findViewById(R.id.et100After);
        
        tv500Used = findViewById(R.id.tv500Used);
        tv200Used = findViewById(R.id.tv200Used);
        tv100Used = findViewById(R.id.tv100Used);
        tvTotal = findViewById(R.id.tvTotal);

        Button btnCalculate = findViewById(R.id.btnCalculate);
        btnCalculate.setOnClickListener(v -> calculate());

        Button btnReset2 = findViewById(R.id.btnReset2);
        btnReset2.setOnClickListener(v -> resetAll());
    }
    
       private void calculate() {
        try {
            // Get values
            int before500 = parseInt(et500Before.getText().toString());
            int after500 = parseInt(et500After.getText().toString());
            int before200 = parseInt(et200Before.getText().toString());
            int after200 = parseInt(et200After.getText().toString());
            int before100 = parseInt(et100Before.getText().toString());
            int after100 = parseInt(et100After.getText().toString());

            // Calculate used notes
            int used500 = after500 - before500;
            int used200 = after200 - before200;
            int used100 = after100 - before100;

            // Validate
            if(used500 < 0 || used200 < 0 || used100 < 0) {
                showError("After count cannot exceed before count!");
                return;
            }

            // Calculate total
            int total = (used500 * 500) + (used200 * 200) + (used100 * 100);
            
            // Display results
            showResults(used500, used200, used100, total);

        } catch (NumberFormatException e) {
            showError("Invalid input! Use numbers only");
        }
    }

    private int parseInt(String input) {
        return input.isEmpty() ? 0 : Integer.parseInt(input);
    }

    private void showResults(int used500, int used200, int used100, int total) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        
        tv500Used.setText(String.format("500 Notes Used: %d", used500));
        tv200Used.setText(String.format("200 Notes Used: %d", used200));
        tv100Used.setText(String.format("100 Notes Used: %d", used100));
        tvTotal.setText(String.format("Total Loaded: %s", format.format(total)));
    }

    private void resetAll() {
        // Clear all inputs
        et500Before.setText("");
        et500After.setText("");
        et200Before.setText("");
        et200After.setText("");
        et100Before.setText("");
        et100After.setText("");

        // Clear results
        tv500Used.setText("");
        tv200Used.setText("");
        tv100Used.setText("");
        tvTotal.setText("");

        Toast.makeText(this, "All fields reset", Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        tvTotal.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        tvTotal.setText(message);
    }


    private void addToTotals() {
        try {
            int new500 = parseInput(etNew500.getText().toString());
            int new200 = parseInput(etNew200.getText().toString());
            int new100 = parseInput(etNew100.getText().toString());

            // Add to cumulative totals
            total500 += new500;
            total200 += new200;
            total100 += new100;

            updateDisplay();
            clearInputFields();

        } catch (NumberFormatException e) {
            tvGrandTotal.setText("Invalid input! Use numbers only");
        }
    }
    
    // Add this new method
private void resetTotals() {
    total500 = 0;
    total200 = 0;
    total100 = 0;
    
    // Update display to show zeros
    tvTotal500.setText("₹500 Notes: 0");
    tvTotal200.setText("₹200 Notes: 0");
    tvTotal100.setText("₹100 Notes: 0");
    
    // Reset grand total display
    NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    tvGrandTotal.setText("Total Amount: " + format.format(0));
    
    // Clear any input fields
    clearInputFields();
    
    Toast.makeText(this, "All totals reset!", Toast.LENGTH_SHORT).show();
}

    private int parseInput(String input) {
        if(input.isEmpty()) return 0;
        int value = Integer.parseInt(input);
        if(value < 0) throw new NumberFormatException();
        return value;
    }

    private void updateDisplay() {
        // Update note counts
        tvTotal500.setText(String.format("₹500 Notes: %d", total500));
        tvTotal200.setText(String.format("₹200 Notes: %d", total200));
        tvTotal100.setText(String.format("₹100 Notes: %d", total100));

        // Calculate and format total amount
        int totalAmount = (total500 * 500) + (total200 * 200) + (total100 * 100);
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        tvGrandTotal.setText("Total Amount: " + format.format(totalAmount));
    }

    private void clearInputFields() {
        etNew500.setText("");
        etNew200.setText("");
        etNew100.setText("");
    }
}