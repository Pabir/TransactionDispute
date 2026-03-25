package com.example.transactiondispute.api;

import com.example.transactiondispute.models.AuthModels;
import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseService {

    @Headers({
        "Content-Type: application/json",
        "X-Client-Info: supabase-java/1.0.0"
    })
    @POST("auth/v1/token?grant_type=password")
    Call<AuthModels.AuthResponse> login(@Body AuthModels.LoginRequest request);

    @GET("rest/v1/profiles")
    Call<List<AuthModels.Profile>> getProfile(
        @Header("Authorization") String authHeader,
        @Header("Range") String range,
        @Query("id") String userId
    );

    @Headers({
        "Content-Type: application/json",
        "Prefer: return=minimal"
    })
    @POST("rest/v1/transactions")
    Call<ResponseBody> insertTransaction(
        @Header("Authorization") String authHeader,
        @Body Map<String, Object> transaction
    );

    @GET("rest/v1/cash_management")
    Call<List<Map<String, Object>>> getCashData(
        @Header("Authorization") String authHeader,
        @Query("select") String select
    );

    @Headers({
        "Content-Type: application/json",
        "Prefer: return=minimal"
    })
    @POST("rest/v1/cash_management")
    Call<ResponseBody> insertCashData(
        @Header("Authorization") String authHeader,
        @Body Map<String, Object> cashData
    );
}
