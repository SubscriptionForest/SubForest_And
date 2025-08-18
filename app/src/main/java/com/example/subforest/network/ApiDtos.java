package com.example.subforest.network;

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
        public long id;
        public String serviceName;
        public Integer amount;
        public String startDate;       // yyyy-MM-dd
        public Integer repeatCycleDays;
        public Boolean autoPayment;
        public Boolean isShared;
        public String logoUrl;         // 서버가 내려주면 사용
        public String nextPaymentDate; // 서버가 내려주면 사용
    }

    public static class SubscriptionListItemDto {
        public long id;
        public String serviceName;
        public Integer amount;
        public String nextPaymentDate;
        public String logoUrl; // 있으면 사용, 없으면 상세에서 보강
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