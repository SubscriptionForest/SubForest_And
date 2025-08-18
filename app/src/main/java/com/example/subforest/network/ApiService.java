package com.example.subforest.network;

import com.example.subforest.network.ApiDtos.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // -------- Auth --------
    @POST("auth/signup")  Call<UserProfile> signup(@Body SignupRequest body);
    @POST("auth/login")   Call<LoginResponse> login(@Body LoginRequest body);
    @POST("auth/logout")  Call<Void> logout();
    @DELETE("auth/withdraw") Call<Void> withdraw();
    @GET("auth/me")       Call<UserProfile> me();
    @PATCH("auth/me")     Call<UserProfile> patchMe(@Body Map<String, Object> partial);

    // -------- Subscriptions --------
    @POST("subscriptions") Call<SubscriptionResponse> createSubscription(@Body SubscriptionRequest body);

    @PUT("subscriptions/{id}") Call<SubscriptionResponse> updateSubscription(@Path("id") long id,
                                                                             @Body SubscriptionRequest body);

    @DELETE("subscriptions/{id}") Call<Void> deleteSubscription(@Path("id") long id);

    @GET("subscriptions") Call<PagedList<SubscriptionListItemDto>> getSubscriptions(@Query("userId") long userId,
                                                                                    @Query("page") int page,
                                                                                    @Query("size") int size);

    @GET("subscriptions/{id}") Call<SubscriptionResponse> getSubscription(@Path("id") long id);

    @GET("subscriptions/upcoming") Call<PagedList<SubscriptionListItemDto>> getUpcoming(@Query("userId") long userId,
                                                                                        @Query("page") int page,
                                                                                        @Query("size") int size);

    // -------- Custom Services --------
    @POST("custom-services") Call<CustomServiceItem> createCustomService(@Body CustomServiceCreate body);

    @GET("custom-services") Call<List<CustomServiceItem>> getCustomServices(@Query("userId") long userId);

    // -------- Services Search --------
    @GET("services/search") Call<List<ServiceItem>> searchServices(@Query("q") String q);

    // -------- Dashboard --------
    @GET("dashboard/summary") Call<DashboardSummary> getSummary(@Query("userId") long userId);
}