package com.example.subforest.network;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    // Auth
    @POST("/auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest req);
    @POST("/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest req);

    // Services
    @GET("/services")
    Call<ApiResponse<List<ServiceDto>>> getServices(@Query("q") String keyword);

    // Custom Service(로고 선택)
    @Multipart
    @POST("/custom-services")
    Call<ApiResponse<CustomServiceDto>> createCustomService(
            @Part("name") RequestBody name,
            @Part MultipartBody.Part logo // null 가능
    );

    // Subscriptions
    @GET("/subscriptions")
    Call<ApiResponse<List<SubscriptionDto>>> getSubscriptions();
    @POST("/subscriptions")
    Call<ApiResponse<SubscriptionDto>> createSubscription(@Body CreateSubscriptionRequest req);
    @PATCH("/subscriptions/{id}")
    Call<ApiResponse<SubscriptionDto>> updateSubscription(
            @Path("id") long id,
            @Body UpdateSubscriptionRequest req
    );
    @DELETE("/subscriptions/{id}")
    Call<ApiResponse<Void>> deleteSubscription(@Path("id") long id);

    // My Page
    @GET("/users/me")
    Call<ApiResponse<UserDto>> getMe();
    @PATCH("/users/me")
    Call<ApiResponse<UserDto>> updateProfile(@Body UpdateProfileRequest req);
    @PATCH("/users/me/password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest req);
    @PATCH("/users/me/notification")
    Call<ApiResponse<UserDto>> updateNotification(@Body NotificationSettingRequest req);
    @DELETE("/users/me")
    Call<ApiResponse<Void>> deleteMe();
}