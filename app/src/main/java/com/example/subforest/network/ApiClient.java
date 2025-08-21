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
                    final TokenStore store = new TokenStore(ctx.getApplicationContext());

                    // Authorization 헤더 자동 부착 (로그인/회원가입 등 공개 엔드포인트는 제외)
                    Interceptor auth = chain -> {
                        Request original = chain.request();
                        String path = original.url().encodedPath();

                        boolean isPublic =
                                path.equals("/auth/login") ||
                                        path.equals("/auth/signup") ||
                                        path.startsWith("/v3/api-docs") ||
                                        path.startsWith("/swagger-ui") ||
                                        "OPTIONS".equalsIgnoreCase(original.method());

                        Request.Builder builder = original.newBuilder()
                                .header("Accept", "application/json");

                        if (!isPublic) {
                            String token = store.getAccessToken();
                            if (token != null) {
                                // 토큰 문자열 정리 (따옴표/공백/중복 Bearer 제거, mock-token 차단)
                                String clean = token.trim().replace("\"", "").replace("'", "");
                                if (clean.regionMatches(true, 0, "Bearer ", 0, 7)) {
                                    clean = clean.substring(7).trim();
                                }
                                if (!clean.isEmpty() && !"mock-token".equalsIgnoreCase(clean)) {
                                    builder.header("Authorization", "Bearer " + clean);
                                }
                            }
                        }

                        return chain.proceed(builder.build());
                    };

                    // HTTP 로깅 (디버그 시 BODY, 릴리스 시 BASIC 권장)
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BODY
                            : HttpLoggingInterceptor.Level.BASIC);

                    OkHttpClient.Builder http = new OkHttpClient.Builder()
                            .addInterceptor(auth)
                            .addInterceptor(logging)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS);

                    // 목 인터셉터
                    if (BuildConfig.USE_MOCK) {
                        http.addInterceptor(new MockInterceptor());
                    }

                    OkHttpClient client = http.build();

                    Gson gson = new GsonBuilder()
                            .setLenient()  // 서버가 살짝 관대한 JSON일 때 대비
                            .create();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.BASE_URL) // 예: http://10.0.2.2:8080/
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