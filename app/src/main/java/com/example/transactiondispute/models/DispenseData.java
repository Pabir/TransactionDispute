package com.example.transactiondispute.models;

import com.google.gson.annotations.SerializedName;

public class DispenseData {
    @SerializedName("id")
    public String id;
    
    @SerializedName("user_id")
    public String userId;
    
    @SerializedName("franchisee_id")
    public String franchiseeId;
    
    @SerializedName("entry_date")
    public String entryDate;
    
    @SerializedName("indent_amount")
    public int indentAmount;
    
    @SerializedName("dispense_amount")
    public int dispenseAmount;
    
    @SerializedName("num_transactions")
    public int numTransactions;

    public DispenseData(String userId, String franchiseeId, String entryDate, int indentAmount, int dispenseAmount, int numTransactions) {
        this.userId = userId;
        this.franchiseeId = franchiseeId;
        this.entryDate = entryDate;
        this.indentAmount = indentAmount;
        this.dispenseAmount = dispenseAmount;
        this.numTransactions = numTransactions;
    }
}
