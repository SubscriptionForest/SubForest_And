package com.example.subforest.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}

    // ===== 공통 페이지 응답 =====
    public static class PagedList<T> {
        public List<T> content;
        public int totalElements;
        public int totalPages;
    }

    // ===== 서비스/커스텀 서비스 =====
    public static class ServiceItem {
        public long id;
        public String name;
        public String logoUrl;
    }

    public static class CustomServiceItem {
        public long id;
        public Long userId;
        public String name;
        public String logoUrl;
    }

    public static class CustomServiceCreate {
        public long userId;
        public String name;
        public String logoUrl; // 파일 업로드 없을 땐 null
    }

    // ===== 구독 =====
    public static class SubscriptionRequest {
        public Long userId;
        public Long serviceId;       // nullable
        public Long customServiceId; // nullable
        public Integer amount;
        public String startDate;       // yyyy-MM-dd
        public Integer repeatCycleDays;
        public Boolean autoPayment;
        public Boolean isShared;
    }

    public static class SubscriptionResponse {
        @SerializedName(value="id", alternate={"subscriptionId","subId","subscription_id"})
        public Long id;

        @SerializedName(value="serviceName", alternate={"name"})
        public String serviceName;

        @SerializedName("logoUrl")
        public String logoUrl;

        @SerializedName("amount")
        public Integer amount;

        @SerializedName(value="repeatCycleDays", alternate={"repeatDays","repeat_cycle_days"})
        public Integer repeatCycleDays;

        @SerializedName("startDate")
        public String startDate;

        @SerializedName("autoPayment")
        public Boolean autoPayment;

        @SerializedName(value="isShared", alternate={"shared"})
        public Boolean isShared;
    }

    public static class SubscriptionListItemDto {
        @SerializedName(value="id", alternate={"subscriptionId","subId","subscription_id"})
        public Long id;

        @SerializedName(value="serviceName", alternate={"name"})
        public String serviceName;

        @SerializedName("logoUrl")
        public String logoUrl;

        @SerializedName("amount")
        public Integer amount;

        @SerializedName(value="repeatCycleDays", alternate={"repeatDays","repeat_cycle_days"})
        public Integer repeatCycleDays;

        @SerializedName(value="nextBillingDate", alternate={"nextDate"})
        public String nextBillingDate;

        @SerializedName("remainingDays")
        public Long remainingDays;

        @SerializedName("autoPayment")
        public Boolean autoPayment;

        @SerializedName(value="shared", alternate={"isShared"})
        public Boolean shared;
    }

    // ===== 마이페이지 =====
    public static class UserProfile {
        public long id;
        public String email;
        public String name;
        public boolean notificationEnabled;
        public String status;
    }

    public static class MessageResponse {
        public String message;
    }

    public static class ChangePasswordReq {
        public String oldPassword;
        public String newPassword;
        public ChangePasswordReq(String oldPassword, String newPassword) {
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
        }
    }

    public static class NotificationToggleReq {
        public boolean notificationEnabled;
        public NotificationToggleReq(boolean enabled) { this.notificationEnabled = enabled; }
    }
    public static class NotificationToggleRes {
        public boolean notificationEnabled;
    }
}