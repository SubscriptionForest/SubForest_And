package com.example.subforest.network;

class ApiResponse<T> {
    boolean success;
    String message;
    T data;
}

// -------------------- Auth --------------------
class LoginRequest {
    String email, password;
    LoginRequest(String email, String password) {
        this.email = email; this.password = password;
    }
}

class RegisterRequest {
    String name, email, password;
    RegisterRequest(String name, String email, String password) {
        this.name = name; this.email = email; this.password = password;
    }
}

class AuthResponse {
    String token;
    UserDto user;
}

// -------------------- User/MyPage --------------------
class UserDto {
    long id;
    String name, email, status;
    boolean email_verified, notification_enabled;
}

class UpdateProfileRequest {
    String name, email;
    UpdateProfileRequest(String name, String email) {
        this.name = name; this.email = email;
    }
}

class ChangePasswordRequest {
    String currentPassword, newPassword;
    ChangePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword; this.newPassword = newPassword;
    }
}

class NotificationSettingRequest {
    boolean enabled;
    NotificationSettingRequest(boolean enabled) { this.enabled = enabled; }
}

// -------------------- Services --------------------
class ServiceDto {
    long id;
    String name, logo_url;
}

class CustomServiceDto {
    long id, user_id;
    String name, logo_url;
}

// -------------------- Subscriptions --------------------
class SubscriptionDto {
    long id, user_id;
    Long service_id, custom_service_id; // nullable
    int amount;
    String start_date;      // yyyy-MM-dd
    int repeat_cycle_days;
    boolean auto_payment, is_shared;
    String created_at;

    // UI 편의(서버에서 조인해서 내려줄 경우)
    String service_name, logo_url;
}

class CreateSubscriptionRequest {
    Long service_id, custom_service_id;     // 둘 중 하나만 채움
    int amount;
    String start_date;      // yyyy-MM-dd
    int repeat_cycle_days;
    boolean auto_payment, is_shared;

    CreateSubscriptionRequest(Long serviceId, Long customServiceId, int amount,
                              String startDate, int repeatCycleDays,
                              boolean autoPayment, boolean isShared) {
        this.service_id = serviceId;
        this.custom_service_id = customServiceId;
        this.amount = amount;
        this.start_date = startDate;
        this.repeat_cycle_days = repeatCycleDays;
        this.auto_payment = autoPayment;
        this.is_shared = isShared;
    }
}

class UpdateSubscriptionRequest {
    Long service_id, custom_service_id; // 둘 중 하나
    int amount;
    String start_date;
    int repeat_cycle_days;
    boolean auto_payment, is_shared;

    UpdateSubscriptionRequest(Long serviceId, Long customServiceId, int amount,
                              String startDate, int repeatCycleDays,
                              boolean autoPayment, boolean isShared) {
        this.service_id = serviceId;
        this.custom_service_id = customServiceId;
        this.amount = amount;
        this.start_date = startDate;
        this.repeat_cycle_days = repeatCycleDays;
        this.auto_payment = autoPayment;
        this.is_shared = isShared;
    }
}