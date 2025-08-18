package com.example.subforest.api;

import com.example.subforest.model.DashboardSummaryResponse;
import com.example.subforest.model.LoginRequest;
import com.example.subforest.model.LoginResponse;
import com.example.subforest.model.RegisterRequest;
import com.example.subforest.model.RegisterResponse;
import com.example.subforest.model.UpcomingSubscriptionResponse;
import com.example.subforest.model.SubscriptionsListResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.DELETE;

public interface ApiService {
    // 1. 회원가입 및 로그인 API
    @POST("/auth/signup")
    Call<RegisterResponse> registerUser(@Body RegisterRequest request);

    @POST("/auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    // 2. 홈 화면 API
    @GET("/dashboard/summary")
    Call<DashboardSummaryResponse> getDashboardSummary(@Query("userId") long userId);

    @GET("/subscriptions/upcoming")
    Call<UpcomingSubscriptionResponse> getUpcomingSubscriptions(@Query("userId") long userId);

    @GET("/subscriptions")
    Call<SubscriptionsListResponse> getSubscriptionsList(@Query("userId") long userId);

    // 3. 내 정보 페이지 API
    @POST("/user/notification")
    Call<Void> updateNotification(@Query("enabled") boolean enabled);

    @POST("/user/deactivate")
    Call<Void> deactivateAccount();

    @POST("/user/change-password")
    @FormUrlEncoded
    Call<Void> changePassword(
            @Field("current") String current,
            @Field("new") String newPassword
    );
}