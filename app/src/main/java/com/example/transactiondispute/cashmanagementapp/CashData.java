package com.example.transactiondispute.cashmanagementapp;

public class CashData {
    private String date;
    private int indentAmount;
    private int sum500Notes;
    private int sum200Notes;
    private int sum100Notes;
    private String eodReceivedFromPurge;
    private int eod500Notes;
    private int eod200Notes;
    private int eod100Notes;
    private String loadingTime;
    
    // Calculated values
    private int sumLoadAmount;
    private int sumEodAmount;
    private int shortLoadAmount;
    private int dueEodAmount;
    private int carryForwardAmount;

    public CashData() {}

    public CashData(String date, int indentAmount, int sum500Notes, int sum200Notes, 
                   int sum100Notes, String eodReceivedFromPurge, int eod500Notes, 
                   int eod200Notes, int eod100Notes, String loadingTime) {
        this.date = date;
        this.indentAmount = indentAmount;
        this.sum500Notes = sum500Notes;
        this.sum200Notes = sum200Notes;
        this.sum100Notes = sum100Notes;
        this.eodReceivedFromPurge = eodReceivedFromPurge;
        this.eod500Notes = eod500Notes;
        this.eod200Notes = eod200Notes;
        this.eod100Notes = eod100Notes;
        this.loadingTime = loadingTime;
        
        calculateDerivedValues();
    }

    private void calculateDerivedValues() {
        this.sumLoadAmount = (this.sum500Notes * 500) + (this.sum200Notes * 200) + (this.sum100Notes * 100);
        this.sumEodAmount = (this.eod500Notes * 500) + (this.eod200Notes * 200) + (this.eod100Notes * 100);
        
        // Short Load = Indent Amount - Actual Load Amount
        // User requested: if indent is 0 but loading happened, it should be negative (short load)
        this.shortLoadAmount = this.indentAmount - this.sumLoadAmount;
        
        // Due EOD = EOD Received from Purge - Actual EOD Amount
        int eodReceived = 0;
        try {
            if (this.eodReceivedFromPurge != null && !this.eodReceivedFromPurge.isEmpty()) {
                eodReceived = Integer.parseInt(this.eodReceivedFromPurge);
            }
        } catch (NumberFormatException e) {
            eodReceived = 0;
        }
        this.dueEodAmount = eodReceived - this.sumEodAmount;

        // Carry Forward = Short Load + Due EOD
        this.carryForwardAmount = this.shortLoadAmount + this.dueEodAmount;
    }

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; calculateDerivedValues(); }
    
    public int getIndentAmount() { return indentAmount; }
    public void setIndentAmount(int indentAmount) { this.indentAmount = indentAmount; calculateDerivedValues(); }
    
    public int getSum500Notes() { return sum500Notes; }
    public void setSum500Notes(int sum500Notes) { this.sum500Notes = sum500Notes; calculateDerivedValues(); }
    
    public int getSum200Notes() { return sum200Notes; }
    public void setSum200Notes(int sum200Notes) { this.sum200Notes = sum200Notes; calculateDerivedValues(); }
    
    public int getSum100Notes() { return sum100Notes; }
    public void setSum100Notes(int sum100Notes) { this.sum100Notes = sum100Notes; calculateDerivedValues(); }
    
    public String getEodReceivedFromPurge() { return eodReceivedFromPurge; }
    public void setEodReceivedFromPurge(String eodReceivedFromPurge) { this.eodReceivedFromPurge = eodReceivedFromPurge; calculateDerivedValues(); }
    
    public int getEod500Notes() { return eod500Notes; }
    public void setEod500Notes(int eod500Notes) { this.eod500Notes = eod500Notes; calculateDerivedValues(); }
    
    public int getEod200Notes() { return eod200Notes; }
    public void setEod200Notes(int eod200Notes) { this.eod200Notes = eod200Notes; calculateDerivedValues(); }
    
    public int getEod100Notes() { return eod100Notes; }
    public void setEod100Notes(int eod100Notes) { this.eod100Notes = eod100Notes; calculateDerivedValues(); }
    
    public String getLoadingTime() { return loadingTime; }
    public void setLoadingTime(String loadingTime) { this.loadingTime = loadingTime; }
    
    public int getSumLoadAmount() { return sumLoadAmount; }
    public int getSumEodAmount() { return sumEodAmount; }
    public int getShortLoadAmount() { return shortLoadAmount; }
    public int getDueEodAmount() { return dueEodAmount; }
    public int getCarryForwardAmount() { return carryForwardAmount; }
}
