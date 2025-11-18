package com.example.transactiondispute.cashmanagementapp;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class DataRepository {
    private Context context;
    
    public DataRepository(Context context) {
        this.context = context;
    }
    
    public List<CashData> getCashData() {
        return DataManager.getInstance(context).getCashDataList();
    }
    
    public void saveCashData(CashData cashData) {
        DataManager.getInstance(context).saveCashData(cashData);
    }
    
    public CashData getCashDataByDate(String date) {
        return DataManager.getInstance(context).getCashDataByDate(date);
    }
    
    public void deleteCashData(String date) {
        DataManager.getInstance(context).deleteCashData(date);
    }
    
    // Get sample data only for first-time setup
    public List<CashData> getSampleData() {
        List<CashData> dataList = new ArrayList<>();
        
        // Only return sample data if no real data exists
        if (DataManager.getInstance(context).getCashDataList().isEmpty()) {
            dataList.add(new CashData(
                "2025-11-01",
                0,
                0, 0, 0,
                "0", 0, 0, 0,
                "00:00:00"
            ));
            
            dataList.add(new CashData(
                "2025-11-02", 
                0,
                0, 0, 0,
                "0", 0, 0, 0,
                "00:00:00"
            ));
            
            dataList.add(new CashData(
                "2025-11-03",
                0,
                0, 0, 0,
                "0", 0, 0, 0,
                "00:00:00"
            ));
            
            // Save sample data
            DataManager.getInstance(context).saveCashDataList(dataList);
        }
        
        return DataManager.getInstance(context).getCashDataList();
    }
}