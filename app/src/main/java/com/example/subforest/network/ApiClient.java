package com.example.subforest.network;

import android.content.Context;

import com.example.subforest.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static Retrofit retrofit;

    private ApiClient() {}

    public static Retrofit get(Context context) {
        if (retrofit == null) {
            OkHttpClient.Builder ok = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            ok.addInterceptor(logging);

            // 디버그에서만 목 응답
            if (BuildConfig.USE_MOCK) {
                ok.addInterceptor(new MockInterceptor(context));
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)   // Gradle
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(ok.build())
                    .build();
        }
        return retrofit;
    }
}