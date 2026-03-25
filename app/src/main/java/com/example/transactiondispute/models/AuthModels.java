package com.example.transactiondispute.models;

import com.google.gson.annotations.SerializedName;

public class AuthModels {
    
    public static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class AuthResponse {
        @SerializedName("access_token")
        public String accessToken;
        @SerializedName("user")
        public User user;
    }

    public static class User {
        public String id;
        public String email;
    }

    public static class Profile {
        public String id;
        @SerializedName("franchisee_id")
        public String franchiseeId;
        public String role;
        @SerializedName("full_name")
        public String fullName;
    }
}
