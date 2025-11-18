package com.example.transactiondispute.cashmanagementapp;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String PREF_NAME = "CashDataPrefs";
    private static final String KEY_CASH_DATA = "cash_data_list";
    private static DataManager instance;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    
    private DataManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }
    
    // Save cash data list
    public void saveCashDataList(List<CashData> cashDataList) {
        String json = gson.toJson(cashDataList);
        sharedPreferences.edit().putString(KEY_CASH_DATA, json).apply();
    }
    
    // Get cash data list
    public List<CashData> getCashDataList() {
        String json = sharedPreferences.getString(KEY_CASH_DATA, null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<CashData>>() {}.getType();
        List<CashData> dataList = gson.fromJson(json, type);
        return dataList != null ? dataList : new ArrayList<>();
    }
    
    // Add or update cash data
    public void saveCashData(CashData newData) {
        List<CashData> dataList = getCashDataList();
        
        // Check if data for this date already exists
        boolean found = false;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getDate().equals(newData.getDate())) {
                dataList.set(i, newData); // Update existing
                found = true;
                break;
            }
        }
        
        if (!found) {
            dataList.add(newData); // Add new
        }
        
        saveCashDataList(dataList);
    }
    
    // Delete cash data by date
    public void deleteCashData(String date) {
        List<CashData> dataList = getCashDataList();
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getDate().equals(date)) {
                dataList.remove(i);
                break;
            }
        }
        saveCashDataList(dataList);
    }
    
    // Get cash data by date
    public CashData getCashDataByDate(String date) {
        List<CashData> dataList = getCashDataList();
        for (CashData data : dataList) {
            if (data.getDate().equals(date)) {
                return data;
            }
        }
        return null;
    }
    
    // Clear all data
    public void clearAllData() {
        sharedPreferences.edit().remove(KEY_CASH_DATA).apply();
    }
}