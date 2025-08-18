package com.example.subforest.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class ApiDtos {

    // ---------- Auth ----------
    public static class SignupRequest { public String email, name, password; }

    public static class LoginRequest {
        public String email, password;
        public LoginRequest() {}
        public LoginRequest(String email, String password) { this.email=email; this.password=password; }
    }

    // { "token": "jwt", "email": "...", "name": "..." , (선택) id }
    public static class LoginResponse { public String token, email, name; public Long id; }

    public static class UserProfile {
        public long id;
        public String email, name;
        public Boolean notificationEnabled; // null-safe 처리 필요
    }

    // ---------- Services / Custom ----------
    public static class ServiceItem {
        public long id;
        public String name;
        public String logoUrl;
    }

    public static class CustomServiceCreate {
        public Long userId;
        public String name;
        public String logoUrl; // 파일업로드 미정 → null 허용
    }

    public static class CustomServiceItem {
        public long id;
        public long userId;
        public String name;
        public String logoUrl;
    }

    // ---------- Subscriptions ----------
    public static class SubscriptionRequest {
        public Long userId;
        public Long serviceId;       // nullable
        public Long customServiceId; // nullable
        public int amount;
        public String startDate;     // yyyy-MM-dd
        @SerializedName("repeatCycleDays") public int repeatCycleDays;
        public boolean autoPayment;
        @SerializedName("isShared")  public boolean isShared;
    }

    public static class SubscriptionResponse {
        public long id;
        @SerializedName("serviceName") public String serviceName;
        public Integer amount;
        public String startDate;
        @SerializedName("repeatCycleDays") public Integer repeatCycleDays;
        public Boolean autoPayment;
        @SerializedName("isShared") public Boolean isShared;
        public String logoUrl; // 선택
    }

    public static class SubscriptionListItemDto {
        public long id;
        @SerializedName("serviceName") public String serviceName;
        public int amount;
        public String nextPaymentDate; // yyyy-MM-dd
        public String logoUrl;
    }

    public static class PagedList<T> {
        public List<T> content;
        public int totalPages;
        public long totalElements;
    }

    // ---------- Dashboard ----------
    public static class DashboardSummary {
        public int totalAmount;
        public int subscriptionCount;
        public Map<String,Integer> chartData;
    }
}