package com.example.subforest.network;

import android.content.Context;
import com.example.subforest.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static volatile Retrofit retrofit;

    public static Retrofit get(Context ctx) {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    TokenStore store = new TokenStore(ctx.getApplicationContext());
                    Interceptor auth = chain -> {
                        Request o = chain.request();
                        Request.Builder b = o.newBuilder().header("Accept", "application/json");
                        String t = store.getAccessToken();
                        if (t != null && !t.isEmpty()) b.header("Authorization", "Bearer " + t);
                        return chain.proceed(b.build());
                    };
                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                            .addInterceptor(auth)
                            .addInterceptor(log)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS);

                    if (BuildConfig.USE_MOCK) {
                        clientBuilder.addInterceptor(new MockInterceptor());
                    }

                    OkHttpClient client = clientBuilder.build();

                    Gson gson = new GsonBuilder().setLenient().create();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static ApiService service(Context ctx) {
        return get(ctx).create(ApiService.class);
    }
}