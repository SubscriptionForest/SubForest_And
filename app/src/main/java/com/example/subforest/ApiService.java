package com.example.subforest;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("user/notification")
    Call<Void> updateNotification(@Query("enabled") boolean enabled);

    @POST("user/deactivate")
    Call<Void> deactivateAccount();

    @POST("user/change-password")
    @FormUrlEncoded
    Call<Void> changePassword(
            @Field("current") String current,
            @Field("new") String newPassword
    );
}
