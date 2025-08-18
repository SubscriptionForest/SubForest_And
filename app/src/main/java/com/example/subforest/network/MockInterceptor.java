package com.example.subforest.network;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Debug 빌드에서 백엔드 없이 동작하도록 가짜 JSON 응답을 주는 인터셉터
 * ApiService의 엔드포인트/스키마와 맞춰둠 (ApiResponse<T> 래퍼)
 */
public final class MockInterceptor implements Interceptor {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final Pattern SUB_ID = Pattern.compile("^/subscriptions/\\d+$");

    private final Context app;

    public MockInterceptor(Context context) {
        this.app = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request req = chain.request();
        String path = req.url().encodedPath();
        String method = req.method();

        // 살짝 지연 줘서 리얼하게
        try { TimeUnit.MILLISECONDS.sleep(300); } catch (InterruptedException ignored) {}

        String body = null;
        int code = 200;

        // ====== AUTH ======
        if (path.equals("/auth/login") && method.equals("POST")) {
            body = "{ \"success\":true, \"message\":null, \"data\":{"
                    + "\"token\":\"mock-token-123\","
                    + "\"user\":{"
                    + "\"id\":1, \"name\":\"홍길동\", \"email\":\"hong@example.com\","
                    + "\"email_verified\":true, \"status\":\"ACTIVE\", \"notification_enabled\":true"
                    + "}}}";
        }
        else if (path.equals("/auth/register") && method.equals("POST")) {
            body = "{ \"success\":true, \"message\":null, \"data\":{"
                    + "\"token\":\"mock-token-456\","
                    + "\"user\":{"
                    + "\"id\":2, \"name\":\"새유저\", \"email\":\"new@example.com\","
                    + "\"email_verified\":false, \"status\":\"ACTIVE\", \"notification_enabled\":true"
                    + "}}}";
        }

        // ====== SERVICES ======
        else if (path.equals("/services") && method.equals("GET")) {
            body = "{ \"success\":true, \"message\":null, \"data\": ["
                    + "{\"id\":101, \"name\":\"Netflix\",  \"logo_url\":null},"
                    + "{\"id\":102, \"name\":\"YouTube\",  \"logo_url\":null},"
                    + "{\"id\":103, \"name\":\"Disney+\",  \"logo_url\":null}"
                    + "]}";
        }
        else if (path.equals("/custom-services") && method.equals("POST")) {
            // 멀티파트 무시하고 성공만 반환
            body = "{ \"success\":true, \"message\":null, \"data\": {"
                    + "\"id\":201, \"user_id\":1, \"name\":\"커스텀서비스\", \"logo_url\":null }}";
        }

        // ====== SUBSCRIPTIONS ======
        else if (path.equals("/subscriptions") && method.equals("GET")) {
            body = "{ \"success\":true, \"message\":null, \"data\": ["
                    + "{ \"id\":1, \"user_id\":1, \"service_id\":101, \"custom_service_id\":null,"
                    + "  \"amount\":9900, \"start_date\":\"2025-07-25\", \"repeat_cycle_days\":30,"
                    + "  \"auto_payment\":true, \"is_shared\":false, \"created_at\":\"2025-07-01T00:00:00\","
                    + "  \"service_name\":\"Netflix\", \"logo_url\":null },"
                    + "{ \"id\":2, \"user_id\":1, \"service_id\":null, \"custom_service_id\":201,"
                    + "  \"amount\":12900, \"start_date\":\"2025-07-29\", \"repeat_cycle_days\":30,"
                    + "  \"auto_payment\":false, \"is_shared\":true, \"created_at\":\"2025-07-05T00:00:00\","
                    + "  \"service_name\":\"커스텀서비스\", \"logo_url\":null }"
                    + "]}";
        }
        else if (path.equals("/subscriptions") && method.equals("POST")) {
            body = "{ \"success\":true, \"message\":null, \"data\": {"
                    + "\"id\":3, \"user_id\":1, \"service_id\":101, \"custom_service_id\":null,"
                    + "\"amount\":9900, \"start_date\":\"2025-08-10\", \"repeat_cycle_days\":30,"
                    + "\"auto_payment\":true, \"is_shared\":false, \"created_at\":\"2025-08-10T10:00:00\","
                    + "\"service_name\":\"Netflix\", \"logo_url\":null }}";
        }
        else if (SUB_ID.matcher(path).matches() && (method.equals("PUT") || method.equals("PATCH"))) {
            body = "{ \"success\":true, \"message\":null, \"data\": {"
                    + "\"id\":1, \"user_id\":1, \"service_id\":101, \"custom_service_id\":null,"
                    + "\"amount\":15900, \"start_date\":\"2025-07-25\", \"repeat_cycle_days\":30,"
                    + "\"auto_payment\":true, \"is_shared\":false, \"created_at\":\"2025-07-01T00:00:00\","
                    + "\"service_name\":\"Netflix\", \"logo_url\":null }}";
        }
        else if (SUB_ID.matcher(path).matches() && method.equals("DELETE")) {
            body = "{ \"success\":true, \"message\":null, \"data\": null }";
        }

        // ====== MYPAGE ======
        else if (path.equals("/users/me") && method.equals("GET")) {
            body = "{ \"success\":true, \"message\":null, \"data\": {"
                    + "\"id\":1, \"name\":\"홍길동\", \"email\":\"hong@example.com\","
                    + "\"email_verified\":true, \"status\":\"ACTIVE\", \"notification_enabled\":true }}";
        }
        else if (path.equals("/users/me") && method.equals("PATCH")) {
            body = "{ \"success\":true, \"message\":null, \"data\": {"
                    + "\"id\":1, \"name\":\"홍길동\", \"email\":\"hong@example.com\","
                    + "\"email_verified\":true, \"status\":\"ACTIVE\", \"notification_enabled\":true }}";
        }
        else if (path.equals("/users/me/password") && method.equals("PATCH")) {
            body = "{ \"success\":true, \"message\":null, \"data\": null }";
        }
        else if (path.equals("/users/me/notification") && method.equals("PATCH")) {
            body = "{ \"success\":true, \"message\":null, \"data\": {"
                    + "\"id\":1, \"name\":\"홍길동\", \"email\":\"hong@example.com\","
                    + "\"email_verified\":true, \"status\":\"ACTIVE\", \"notification_enabled\":true }}";
        }
        else if (path.equals("/users/me") && method.equals("DELETE")) {
            body = "{ \"success\":true, \"message\":null, \"data\": null }";
        }

        // ====== NOT FOUND ======
        if (body == null) {
            code = 404;
            body = "{ \"success\":false, \"message\":\"Mock: Not Found\", \"data\":null }";
        }

        return new Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(code == 200 ? "OK" : "MOCK")
                .body(ResponseBody.create(body.getBytes(StandardCharsets.UTF_8), JSON))
                .build();
    }
}