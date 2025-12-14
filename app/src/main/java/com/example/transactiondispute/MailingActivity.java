package com.example.transactiondispute;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.LinearLayout;
import android.widget.CheckBox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MailingActivity extends AppCompatActivity {
    
    private Button btnMachineIssues, btnUpsBattery, btnNetworkLink, btnRoomInfrastructure, btnEodDocketRequest;
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
        btnEodDocketRequest = findViewById(R.id.btnEodDocketRequest);
    }
    
    private void setupEventListeners() {
        btnMachineIssues.setOnClickListener(v -> sendMachineIssuesEmail());
        btnUpsBattery.setOnClickListener(v -> sendUpsBatteryEmail());
        btnNetworkLink.setOnClickListener(v -> sendNetworkLinkEmail());
        btnRoomInfrastructure.setOnClickListener(v -> sendRoomInfrastructureEmail());
        btnTransactionDisputes.setOnClickListener(v -> sendTransactionDisputesEmail());
        btnCashManagement.setOnClickListener(v -> sendCashManagementEmail());
        btnOpenGmail.setOnClickListener(v -> openGmailApp());
        btnEodDocketRequest.setOnClickListener(v -> showAtmIdDialogForEod());
    }

    private void showAtmIdDialogForEod() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter ATM ID for EOD Request");
        builder.setMessage("Please enter the ATM ID for EOD docket request:");
        
        // Set up the input
        final EditText input = new EditText(this);
        input.setHint("e.g., ATM001, ATM002, etc.");
        builder.setView(input);
        
        // Set up the buttons
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String atmId = input.getText().toString().trim();
                if (atmId.isEmpty()) {
                    Toast.makeText(MailingActivity.this, "Please enter ATM ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Proceed with EOD email using the entered ATM ID
                sendEodDocketRequestEmail(atmId);
            }
        });
        
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        
        builder.show();
    }

    private void sendEodDocketRequestEmail(String atmId) {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");

            String[] to = {"WLASupport@hitachi-payments.com"};
            String[] cc = {"dibyendu.majumder@hitachi-payments.com", "jagdish.panchal@hitachi-payments.com", "mf.techeasyservices@gmail.com"};

            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_CC, cc);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "EOD Docket Request - " + atmId + " - " + getCurrentDate());
            emailIntent.putExtra(Intent.EXTRA_TEXT, createEodDocketRequestContent(atmId));

            startActivity(Intent.createChooser(emailIntent, "Send EOD Docket Request..."));
            tvEmailStatus.setText("EOD docket request email ready to send");

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String createEodDocketRequestContent(String atmId) {
        return "Dear Team,\n\n"
                + "I am requesting the EOD for\n\n"
                + "• ATM ID: " + atmId + "\n"
                + "• Also share the docket number for the same.\n\n"
                + "• Attached snap below:-\n\n";
    }

    // ALL OTHER METHODS REMAIN EXACTLY THE SAME AS YOUR ORIGINAL CODE
    private void sendMachineIssuesEmail() {
        showMachineIssueDialog();
    }

    private void showMachineIssueDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Report Machine Issue");
        builder.setMessage("Fill all details to report machine issue:");

        // Create the dialog layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        // ATM ID
        TextView tvAtmId = new TextView(this);
        tvAtmId.setText("ATM ID:");
        layout.addView(tvAtmId);

        final EditText etAtmId = new EditText(this);
        etAtmId.setHint("Enter ATM ID");
        etAtmId.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        layout.addView(etAtmId);

        // Machine Issue Type (Dropdown)
        TextView tvIssueType = new TextView(this);
        tvIssueType.setText("Select Issue Type:");
        tvIssueType.setPadding(0, 20, 0, 0);
        layout.addView(tvIssueType);

        final Spinner spIssueType = new Spinner(this);
        ArrayAdapter<String> issueAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        new String[] {
                            "Select Issue Type",
                            "Note Jam/Currency Jam",
                            "Card Reader Error",
                            "ATM Being Serviced",
                            "Cassette Disabled",
                            "Screen Not Working",
                            "Keypad Issue",
                            "Receipt Printer Error",
                            "Cash Dispense Error",
                            "Power Supply Issue",
                            "Software Error",
                            "Other"
                        });
        issueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIssueType.setAdapter(issueAdapter);
        layout.addView(spIssueType);

        // Engineer Name
        TextView tvEngineerName = new TextView(this);
        tvEngineerName.setText("Engineer Name:");
        tvEngineerName.setPadding(0, 20, 0, 0);
        layout.addView(tvEngineerName);

        final EditText etEngineerName = new EditText(this);
        etEngineerName.setHint("Enter engineer name (if contacted)");
        layout.addView(etEngineerName);

        // Engineer Mobile Number
        TextView tvEngineerNumber = new TextView(this);
        tvEngineerNumber.setText("Engineer Mobile Number:");
        layout.addView(tvEngineerNumber);

        final EditText etEngineerNumber = new EditText(this);
        etEngineerNumber.setHint("Enter engineer mobile number");
        etEngineerNumber.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.addView(etEngineerNumber);

        // Support Status (Dropdown)
        TextView tvSupportStatus = new TextView(this);
        tvSupportStatus.setText("Support Status:");
        tvSupportStatus.setPadding(0, 20, 0, 0);
        layout.addView(tvSupportStatus);

        final Spinner spSupportStatus = new Spinner(this);
        ArrayAdapter<String> supportAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        new String[] {
                            "Select Support Status",
                            "Took support from engineer",
                            "Engineer mobile number is not reachable",
                            "Engineer did not respond",
                            "Engineer visited site",
                            "Engineer provided remote support",
                            "No support taken yet",
                            "Support team contacted",
                            "Waiting for engineer response",
                            "Issue escalated to senior engineer"
                        });
        supportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSupportStatus.setAdapter(supportAdapter);
        layout.addView(spSupportStatus);

        // Additional Remarks
        TextView tvRemarks = new TextView(this);
        tvRemarks.setText("Additional Remarks:");
        tvRemarks.setPadding(0, 20, 0, 0);
        layout.addView(tvRemarks);

        final EditText etRemarks = new EditText(this);
        etRemarks.setHint("Enter additional details about the issue");
        etRemarks.setMinLines(3);
        layout.addView(etRemarks);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton(
                "Submit Report",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get all values
                        String atmId = etAtmId.getText().toString().trim();
                        String issueType = spIssueType.getSelectedItem().toString();
                        String engineerName = etEngineerName.getText().toString().trim();
                        String engineerNumber = etEngineerNumber.getText().toString().trim();
                        String supportStatus = spSupportStatus.getSelectedItem().toString();
                        String remarks = etRemarks.getText().toString().trim();

                        // Validate required fields
                        if (atmId.isEmpty()
                                || issueType.equals("Select Issue Type")
                                || supportStatus.equals("Select Support Status")) {
                            Toast.makeText(
                                            MailingActivity.this,
                                            "Please fill all required fields",
                                            Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }

                        // Send email with all details
                        sendMachineIssueEmailWithDetails(
                                atmId,
                                issueType,
                                engineerName,
                                engineerNumber,
                                supportStatus,
                                remarks);
                    }
                });

        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendMachineIssueEmailWithDetails(
            String atmId,
            String issueType,
            String engineerName,
            String engineerNumber,
            String supportStatus,
            String remarks) {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");

            String[] to = {"support@hitachi-payments.com", "technicalsupport@hitachi-payments.com"};
            String[] cc = {"manager@hitachi-payments.com"};

            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_CC, cc);
            emailIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Machine Issue Report - " + atmId + " - " + getCurrentDate());
            emailIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    createMachineIssuesContentWithDetails(
                            atmId,
                            issueType,
                            engineerName,
                            engineerNumber,
                            supportStatus,
                            remarks));

            startActivity(Intent.createChooser(emailIntent, "Send Machine Issue Report..."));
            tvEmailStatus.setText("Machine issue report submitted");

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String createMachineIssuesContentWithDetails(
            String atmId,
            String issueType,
            String engineerName,
            String engineerNumber,
            String supportStatus,
            String remarks) {
        StringBuilder content = new StringBuilder();

        content.append("Dear Technical Support Team,\n\n");
        content.append("I am reporting a machine issue at our ATM location.\n\n");

        content.append("ISSUE DETAILS:\n");
        content.append("• ATM ID: ").append(atmId).append("\n");
        content.append("• Issue Type: ").append(issueType).append("\n");
        content.append("• Date/Time: ").append(getCurrentDateTime()).append("\n");

        if (!engineerName.isEmpty()) {
            content.append("• Engineer Name: ").append(engineerName).append("\n");
        }

        if (!engineerNumber.isEmpty()) {
            content.append("• Engineer Contact: ").append(engineerNumber).append("\n");
        }

        content.append("• Support Status: ").append(supportStatus).append("\n");

        if (!remarks.isEmpty()) {
            content.append("• Additional Remarks: ").append(remarks).append("\n");
        }

        content.append("ACTION REQUESTED:\n");
        content.append("1. Please investigate the issue\n");
        content.append("2. Provide technical support\n");
        content.append("3. Schedule maintenance if required\n");
        content.append("4. Provide estimated resolution time\n\n");

        content.append("Thank you for your prompt attention to this matter.\n\n");

        return content.toString();
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void sendUpsBatteryEmail() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            
            String[] to = {"WLASupport@hitachi-payments.com"};
            String[] cc = {"jagdish.panchal@hitachi-payments.com"};
            
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
        showNetworkIssueDialog();
    }

    private void showNetworkIssueDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Report Network/Link Issue");
        builder.setMessage("Fill all details to report network issue:");

        // Create the dialog layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        // ATM ID
        TextView tvAtmId = new TextView(this);
        tvAtmId.setText("ATM ID:");
        layout.addView(tvAtmId);

        final EditText etAtmId = new EditText(this);
        etAtmId.setHint("Enter ATM ID");
        etAtmId.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        layout.addView(etAtmId);

        // Network Issue Type (Dropdown)
        TextView tvIssueType = new TextView(this);
        tvIssueType.setText("Select Network Issue:");
        tvIssueType.setPadding(0, 20, 0, 0);
        layout.addView(tvIssueType);

        final Spinner spIssueType = new Spinner(this);
        ArrayAdapter<String> issueAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        new String[] {
                            "Select Issue Type",
                            "Showing ATM is being Serviced",
                            "White Screen/Display Issue",
                            "Out of Service",
                            "No Network Connectivity",
                            "Slow Network/High Latency",
                            "Intermittent Connection Drops",
                            "ISP Link Down",
                            "Leased Line Failure",
                            "3G/4G Connectivity Issue",
                            "Router/Switch Problem",
                            "Server Connection Failed",
                            "Network Timeout Errors",
                            "IP Address Conflict",
                            "DNS Resolution Failure",
                            "Other Network Issue"
                        });
        issueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIssueType.setAdapter(issueAdapter);
        layout.addView(spIssueType);

        // Technician Name
        TextView tvTechnicianName = new TextView(this);
        tvTechnicianName.setText("Technician Name:");
        tvTechnicianName.setPadding(0, 20, 0, 0);
        layout.addView(tvTechnicianName);

        final EditText etTechnicianName = new EditText(this);
        etTechnicianName.setHint("Enter technician name (if contacted)");
        layout.addView(etTechnicianName);

        // Technician Mobile Number
        TextView tvTechnicianNumber = new TextView(this);
        tvTechnicianNumber.setText("Technician Mobile Number:");
        layout.addView(tvTechnicianNumber);

        final EditText etTechnicianNumber = new EditText(this);
        etTechnicianNumber.setHint("Enter technician mobile number");
        etTechnicianNumber.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.addView(etTechnicianNumber);

        // ISP Provider (Dropdown)
        TextView tvIspProvider = new TextView(this);
        tvIspProvider.setText("ISP Provider:");
        tvIspProvider.setPadding(0, 20, 0, 0);
        layout.addView(tvIspProvider);

        final Spinner spIspProvider = new Spinner(this);
        ArrayAdapter<String> ispAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        new String[] {
                            "Select ISP Provider",
                            "BSNL",
                            "Airtel",
                            "Jio",
                            "Vodafone Idea",
                            "Tata Communications",
                            "ACT Fibernet",
                            "Hathway",
                            "Spectra",
                            "MTNL",
                            "Other ISP",
                            "Not Applicable"
                        });
        ispAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIspProvider.setAdapter(ispAdapter);
        layout.addView(spIspProvider);

        // Connection Type (Dropdown)
        TextView tvConnectionType = new TextView(this);
        tvConnectionType.setText("Connection Type:");
        layout.addView(tvConnectionType);

        final Spinner spConnectionType = new Spinner(this);
        ArrayAdapter<String> connectionAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        new String[] {
                            "Select Connection Type",
                            "Leased Line",
                            "3G Connection",
                            "4G Connection",
                            "Broadband",
                            "VSAT",
                            "Fiber Optic",
                            "Ethernet",
                            "Wi-Fi",
                            "Not Known"
                        });
        connectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spConnectionType.setAdapter(connectionAdapter);
        layout.addView(spConnectionType);

        // Additional Remarks
        TextView tvRemarks = new TextView(this);
        tvRemarks.setText("Additional Remarks:");
        tvRemarks.setPadding(0, 20, 0, 0);
        layout.addView(tvRemarks);

        final EditText etRemarks = new EditText(this);
        etRemarks.setHint("Enter additional details about network issue");
        etRemarks.setMinLines(3);
        layout.addView(etRemarks);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton(
                "Submit Report",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get all values
                        String atmId = etAtmId.getText().toString().trim();
                        String issueType = spIssueType.getSelectedItem().toString();
                        String technicianName = etTechnicianName.getText().toString().trim();
                        String technicianNumber = etTechnicianNumber.getText().toString().trim();
                        String ispProvider = spIspProvider.getSelectedItem().toString();
                        String connectionType = spConnectionType.getSelectedItem().toString();
                        String remarks = etRemarks.getText().toString().trim();

                        // Validate required fields
                        if (atmId.isEmpty()
                                || issueType.equals("Select Issue Type")
                                || ispProvider.equals("Select ISP Provider")
                                || connectionType.equals("Select Connection Type")) {
                            Toast.makeText(
                                            MailingActivity.this,
                                            "Please fill all required fields",
                                            Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }

                        // Send email with all details
                        sendNetworkIssueEmailWithDetails(
                                atmId,
                                issueType,
                                technicianName,
                                technicianNumber,
                                ispProvider,
                                connectionType,
                                remarks);
                    }
                });

        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendNetworkIssueEmailWithDetails(
            String atmId,
            String issueType,
            String technicianName,
            String technicianNumber,
            String ispProvider,
            String connectionType,
            String remarks) {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");

            String[] to = {"network@hitachi-payments.com", "itsupport@hitachi-payments.com"};
            String[] cc = {"operations@hitachi-payments.com", "support@hitachi-payments.com"};

            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_CC, cc);
            emailIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Network Issue Report - " + atmId + " - " + getCurrentDate());
            emailIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    createNetworkIssueContentWithDetails(
                            atmId,
                            issueType,
                            technicianName,
                            technicianNumber,
                            ispProvider,
                            connectionType,
                            remarks));

            startActivity(Intent.createChooser(emailIntent, "Send Network Issue Report..."));
            tvEmailStatus.setText("Network issue report submitted");

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String createNetworkIssueContentWithDetails(
            String atmId,
            String issueType,
            String technicianName,
            String technicianNumber,
            String ispProvider,
            String connectionType,
            String remarks) {
        StringBuilder content = new StringBuilder();

        content.append("Dear Network Support Team,\n\n");
        content.append("I am reporting a network/link issue at our ATM location.\n\n");

        content.append("NETWORK ISSUE DETAILS:\n");
        content.append("• ATM ID: ").append(atmId).append("\n");
        content.append("• Issue Type: ").append(issueType).append("\n");
        content.append("• Date/Time Reported: ").append(getCurrentDateTime()).append("\n");
        content.append("• ISP Provider: ").append(ispProvider).append("\n");
        content.append("• Connection Type: ").append(connectionType).append("\n");

        if (!technicianName.isEmpty()) {
            content.append("• Technician Name: ").append(technicianName).append("\n");
        }

        if (!technicianNumber.isEmpty()) {
            content.append("• Technician Contact: ").append(technicianNumber).append("\n");
        }

        content.append("• Issue First Noticed: [Please specify time]\n");
        content.append("• Duration of Issue: [Please specify duration]\n");

        if (!remarks.isEmpty()) {
            content.append("• Additional Remarks: ").append(remarks).append("\n");
        }

        content.append("\nIMPACT ASSESSMENT:\n");
        content.append("• Services Affected: [Transactions/Display/Connectivity]\n");
        content.append("• Business Impact: [High/Medium/Low]\n");
        content.append("• Number of Transactions Failed: [Please specify]\n");
        content.append("• Estimated Revenue Loss: [If applicable]\n\n");

        content.append("TROUBLESHOOTING ATTEMPTED:\n");
        content.append("• Router/Switch Restart: [Yes/No]\n");
        content.append("• ISP Contacted: [Yes/No]\n");
        content.append("• Local Connectivity Check: [Yes/No]\n");
        content.append("• Alternative Connection Tested: [Yes/No]\n\n");

        content.append("ACTION REQUESTED:\n");
        content.append("1. Please investigate the network connectivity issue\n");
        content.append("2. Contact ISP provider if required\n");
        content.append("3. Restore network connectivity at the earliest\n");
        content.append("4. Provide status update within [specify hours]\n\n");

        content.append("URGENCY LEVEL:\n");
        content.append("• Priority: [Critical/High/Medium/Low]\n");
        content.append("• Required Resolution Time: [Please specify]\n\n");

        content.append("CONTACT INFORMATION FOR FOLLOW-UP:\n");
        content.append("• Site Contact Person: [Name]\n");
        content.append("• Site Contact Number: [Number]\n");
        content.append("• Best Time to Contact: [Please specify]\n\n");

        content.append("Thank you for your prompt attention to this matter.\n\n");
        content.append("Best regards,\n");
        content.append("ATM Site Manager");

        return content.toString();
    }

    private void sendRoomInfrastructureEmail() {
        showRoomInfrastructureDialog();
    }

    private void showRoomInfrastructureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Report Room/Infrastructure Issue");
        builder.setMessage("Fill all details to report infrastructure issue:");

        // Create the dialog layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        // ATM ID
        TextView tvAtmId = new TextView(this);
        tvAtmId.setText("ATM ID:");
        layout.addView(tvAtmId);

        final EditText etAtmId = new EditText(this);
        etAtmId.setHint("Enter ATM ID");
        etAtmId.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        layout.addView(etAtmId);

        // Infrastructure Issue Type (Dropdown)
        TextView tvIssueType = new TextView(this);
        tvIssueType.setText("Select Infrastructure Issue:");
        tvIssueType.setPadding(0, 20, 0, 0);
        layout.addView(tvIssueType);

        final Spinner spIssueType = new Spinner(this);
        ArrayAdapter<String> issueAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        new String[] {
                            "Select Issue Type",
                            "Door Closer Not Working",
                            "Door Handle Broken",
                            "Lock Not Functioning",
                            "AC Not Working",
                            "Air Conditioning Issue",
                            "Lighting Problem",
                            "Power Socket Faulty",
                            "Water Leakage",
                            "Wall Damage/Cracks",
                            "Flooring Issue",
                            "Ceiling Problem",
                            "Window Broken",
                            "Glass Door Damage",
                            "Ventilation Issue",
                            "Drainage Problem",
                            "Fire Safety Equipment Issue",
                            "CCTV Camera Not Working",
                            "Security Alarm Fault",
                            "UPS Room Issue",
                            "Generator Room Problem",
                            "Parking Area Issue",
                            "Boundary Wall Damage",
                            "Gate Not Working",
                            "Other Infrastructure Issue"
                        });
        issueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIssueType.setAdapter(issueAdapter);
        layout.addView(spIssueType);

        // Location/Specific Area
        TextView tvLocation = new TextView(this);
        tvLocation.setText("Location/Area:");
        tvLocation.setPadding(0, 20, 0, 0);
        layout.addView(tvLocation);

        final Spinner spLocation = new Spinner(this);
        ArrayAdapter<String> locationAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        new String[] {
                            "Select Location",
                            "ATM Room",
                            "ATM Lobby",
                            "Entry Door",
                            "Exit Door",
                            "Main Entrance",
                            "Waiting Area",
                            "Cash Loading Room",
                            "Server Room",
                            "UPS Room",
                            "Generator Area",
                            "Parking Area",
                            "Boundary Wall",
                            "Main Gate",
                            "Security Cabin",
                            "Common Area",
                            "Other Location"
                        });
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLocation.setAdapter(locationAdapter);
        layout.addView(spLocation);

        // Severity Level (Dropdown)
        TextView tvSeverity = new TextView(this);
        tvSeverity.setText("Severity Level:");
        tvSeverity.setPadding(0, 20, 0, 0);
        layout.addView(tvSeverity);

        final Spinner spSeverity = new Spinner(this);
        ArrayAdapter<String> severityAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        new String[] {
                            "Select Severity",
                            "Critical - Immediate Attention Required",
                            "High - Needs Urgent Repair",
                            "Medium - Needs Repair Soon",
                            "Low - Can Wait for Maintenance",
                            "Cosmetic - Not Urgent"
                        });
        severityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSeverity.setAdapter(severityAdapter);
        layout.addView(spSeverity);

        // Maintenance Contractor Name
        TextView tvContractorName = new TextView(this);
        tvContractorName.setText("Maintenance Contractor Name:");
        tvContractorName.setPadding(0, 20, 0, 0);
        layout.addView(tvContractorName);

        final EditText etContractorName = new EditText(this);
        etContractorName.setHint("Enter contractor name (if known)");
        layout.addView(etContractorName);

        // Contractor Contact Number
        TextView tvContractorNumber = new TextView(this);
        tvContractorNumber.setText("Contractor Contact Number:");
        layout.addView(tvContractorNumber);

        final EditText etContractorNumber = new EditText(this);
        etContractorNumber.setHint("Enter contractor contact number");
        etContractorNumber.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.addView(etContractorNumber);

        // Additional Remarks
        TextView tvRemarks = new TextView(this);
        tvRemarks.setText("Additional Remarks/Description:");
        tvRemarks.setPadding(0, 20, 0, 0);
        layout.addView(tvRemarks);

        final EditText etRemarks = new EditText(this);
        etRemarks.setHint("Describe the issue in detail, measurements if required");
        etRemarks.setMinLines(3);
        layout.addView(etRemarks);

        // Safety Concern Checkbox
        final CheckBox cbSafetyConcern = new CheckBox(this);
        cbSafetyConcern.setText("This issue poses a safety/security risk");
        cbSafetyConcern.setPadding(0, 20, 0, 0);
        layout.addView(cbSafetyConcern);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton(
                "Submit Report",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get all values
                        String atmId = etAtmId.getText().toString().trim();
                        String issueType = spIssueType.getSelectedItem().toString();
                        String location = spLocation.getSelectedItem().toString();
                        String severity = spSeverity.getSelectedItem().toString();
                        String contractorName = etContractorName.getText().toString().trim();
                        String contractorNumber = etContractorNumber.getText().toString().trim();
                        String remarks = etRemarks.getText().toString().trim();
                        boolean safetyConcern = cbSafetyConcern.isChecked();

                        // Validate required fields
                        if (atmId.isEmpty()
                                || issueType.equals("Select Issue Type")
                                || location.equals("Select Location")
                                || severity.equals("Select Severity")) {
                            Toast.makeText(
                                            MailingActivity.this,
                                            "Please fill all required fields",
                                            Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }

                        // Send email with all details
                        sendRoomInfrastructureEmailWithDetails(
                                atmId,
                                issueType,
                                location,
                                severity,
                                contractorName,
                                contractorNumber,
                                remarks,
                                safetyConcern);
                    }
                });

        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendRoomInfrastructureEmailWithDetails(
            String atmId,
            String issueType,
            String location,
            String severity,
            String contractorName,
            String contractorNumber,
            String remarks,
            boolean safetyConcern) {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");

            String[] to = {"facility@hitachi-payments.com", "admin@hitachi-payments.com"};
            String[] cc = {"manager@hitachi-payments.com", "security@hitachi-payments.com"};

            if (safetyConcern) {
                cc =
                        new String[] {
                            "safety@hitachi-payments.com",
                            "security@hitachi-payments.com",
                            "manager@hitachi-payments.com"
                        };
            }

            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_CC, cc);
            emailIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Infrastructure Issue - " + atmId + " - " + getCurrentDate());
            emailIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    createRoomInfrastructureContentWithDetails(
                            atmId,
                            issueType,
                            location,
                            severity,
                            contractorName,
                            contractorNumber,
                            remarks,
                            safetyConcern));

            startActivity(Intent.createChooser(emailIntent, "Send Infrastructure Report..."));
            tvEmailStatus.setText("Infrastructure issue report submitted");

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String createRoomInfrastructureContentWithDetails(
            String atmId,
            String issueType,
            String location,
            String severity,
            String contractorName,
            String contractorNumber,
            String remarks,
            boolean safetyConcern) {
        StringBuilder content = new StringBuilder();

        content.append("Dear Facility Management Team,\n\n");

        if (safetyConcern) {
            content.append("⚠️ URGENT: SAFETY/SECURITY CONCERN IDENTIFIED ⚠️\n\n");
        }

        content.append("I am reporting an infrastructure issue at our ATM premises.\n\n");

        content.append("INFRASTRUCTURE ISSUE DETAILS:\n");
        content.append("• ATM ID: ").append(atmId).append("\n");
        content.append("• Issue Type: ").append(issueType).append("\n");
        content.append("• Location/Area: ").append(location).append("\n");
        content.append("• Severity Level: ").append(severity).append("\n");

        if (safetyConcern) {
            content.append("• Safety/Security Risk: YES - Immediate attention required\n");
        }

        content.append("• Date/Time Reported: ").append(getCurrentDateTime()).append("\n");

        if (!contractorName.isEmpty()) {
            content.append("• Maintenance Contractor: ").append(contractorName).append("\n");
        }

        if (!contractorNumber.isEmpty()) {
            content.append("• Contractor Contact: ").append(contractorNumber).append("\n");
        }

        if (!remarks.isEmpty()) {
            content.append("• Issue Description: ").append(remarks).append("\n");
        }

        content.append("\nADDITIONAL INFORMATION:\n");
        content.append("• Exact Location Details: [Please specify exact spot]\n");
        content.append("• Measurements/Dimensions: [If applicable]\n");
        content.append("• Material Type: [Wood/Steel/Glass/Concrete]\n");
        content.append("• Brand/Model: [If known]\n");
        content.append("• Age of Infrastructure: [If known]\n");
        content.append("• Previous Repairs: [Yes/No - Details if yes]\n\n");

        content.append("IMPACT ASSESSMENT:\n");
        content.append("• Operations Impact: [Full/Partial/No impact]\n");
        content.append("• Safety Hazard: [High/Medium/Low/None]\n");
        content.append("• Security Risk: [High/Medium/Low/None]\n");
        content.append("• Customer Convenience: [Affected/Not Affected]\n\n");

        content.append("PHOTOGRAPHIC EVIDENCE:\n");
        content.append("• Photos Attached: [Yes/No]\n");
        content.append("• Video Recording: [Yes/No]\n\n");

        content.append("ACTION REQUESTED:\n");
        content.append("1. Site inspection and assessment\n");
        content.append("2. Temporary safety measures (if required)\n");
        content.append("3. Quotation for repairs/replacement\n");
        content.append("4. Schedule maintenance work\n");

        if (safetyConcern) {
            content.append("5. IMMEDIATE SAFETY MEASURES REQUIRED\n");
        }

        content.append("\nURGENCY & PRIORITY:\n");
        content.append("• Required Action Time: [Immediate/Within 24hrs/48hrs/Week]\n");
        content.append("• Budget Approval Required: [Yes/No]\n");
        content.append("• Estimated Cost Range: [If known]\n\n");

        content.append("CONTACT FOR SITE VISIT:\n");
        content.append("• Site Incharge: [Name]\n");
        content.append("• Contact Number: [Number]\n");
        content.append("• Best Visiting Hours: [Please specify]\n");
        content.append("• Site Access Requirements: [Key/Security Clearance]\n\n");

        content.append("Thank you for your prompt attention to this matter.\n\n");
        content.append("Best regards,\n");
        content.append("ATM Site Manager");

        return content.toString();
    }

    private void sendTransactionDisputesEmail() {
        Intent intent = new Intent(MailingActivity.this, TransactionDisputeActivity.class);
        startActivity(intent);
    }
    
    private void sendCashManagementEmail() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            
            String[] to = {"WLASupport@hitachi-payments.com"};
            String[] cc = {"jagdish.panchal@hitachi-payments.com"};
            
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
    
    // Email Content Templates (ALL REMAIN EXACTLY THE SAME)
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