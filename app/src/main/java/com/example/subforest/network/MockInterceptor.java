package com.example.subforest.network;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 서버 없이 Retrofit 호출을 가짜 JSON으로 응답하는 인터셉터 */
public class MockInterceptor implements Interceptor {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request req = chain.request();
        String path = req.url().encodedPath();
        String method = req.method();
        String mock = "{}";
        int code = HttpURLConnection.HTTP_OK;

        // ---- AUTH ----
        if (path.equals("/auth/login") && method.equals("POST")) {
            mock = "{ \"token\":\"mock-token-123\", \"email\":\"test@example.com\", \"name\":\"홍길동\", \"id\":1 }";
        } else if (path.equals("/auth/me") && method.equals("GET")) {
            mock = "{ \"id\":1, \"email\":\"test@example.com\", \"name\":\"홍길동\", \"notificationEnabled\":true }";
        } else if (path.equals("/auth/signup") && method.equals("POST")) {
            mock = "{ \"id\":1, \"email\":\"test@example.com\", \"name\":\"홍길동\" }";
        } else if (path.equals("/auth/logout") && method.equals("POST")) {
            mock = ""; code = HttpURLConnection.HTTP_NO_CONTENT;
        } else if (path.equals("/auth/withdraw") && method.equals("DELETE")) {
            mock = ""; code = HttpURLConnection.HTTP_NO_CONTENT;

            // ---- DASHBOARD ----
        } else if (path.equals("/dashboard/summary") && method.equals("GET")) {
            mock = "{ \"totalAmount\": 32300, \"subscriptionCount\": 3, " +
                    "  \"chartData\": { \"넷플릭스\": 13500, \"스포티파이\": 7900, \"디즈니+\": 10900 } }";

            // ---- SERVICES / CUSTOM ----
        } else if (path.equals("/services/search") && method.equals("GET")) {
            mock = "[{\"id\":101,\"name\":\"넷플릭스\",\"logoUrl\":\"https://cdn/mock/netflix.png\"}," +
                    " {\"id\":102,\"name\":\"디즈니+\",\"logoUrl\":\"https://cdn/mock/disney.png\"}]";
        } else if (path.equals("/custom-services") && method.equals("GET")) {
            mock = "[{\"id\":201,\"userId\":1,\"name\":\"내서비스\",\"logoUrl\":\"/uploads/custom123.png\"}]";
        } else if (path.equals("/custom-services") && method.equals("POST")) {
            mock = "{ \"id\":202, \"userId\":1, \"name\":\"내서비스(신규)\", \"logoUrl\":\"/uploads/custom456.png\" }";

            // ---- SUBSCRIPTIONS ----
        } else if (path.equals("/subscriptions") && method.equals("GET")) {
            // 쿼리: userId, page, size 무시하고 고정 응답
            mock = "{ \"content\": [" +
                    "  {\"id\":10,\"serviceName\":\"넷플릭스\",\"amount\":13500,\"nextPaymentDate\":\"2025-09-12\",\"logoUrl\":\"https://cdn/mock/netflix.png\"}," +
                    "  {\"id\":11,\"serviceName\":\"디즈니+\",\"amount\":10900,\"nextPaymentDate\":\"2025-09-25\",\"logoUrl\":\"https://cdn/mock/disney.png\"}," +
                    "  {\"id\":12,\"serviceName\":\"스포티파이\",\"amount\":7900,\"nextPaymentDate\":\"2025-09-03\",\"logoUrl\":\"https://cdn/mock/spotify.png\"}" +
                    "], \"totalElements\":3, \"totalPages\":1 }";
        } else if (path.matches("^/subscriptions/\\d+$") && method.equals("GET")) {
            long id = extractId(path);
            mock = "{ \"id\":" + id + "," +
                    "  \"serviceName\":\"" + pickName(id) + "\"," +
                    "  \"amount\": " + pickAmount(id) + "," +
                    "  \"startDate\": \"2025-08-12\"," +
                    "  \"repeatCycleDays\": 30," +
                    "  \"autoPayment\": " + (id % 2 == 0) + "," +
                    "  \"isShared\": " + (id % 3 == 0) + "," +
                    "  \"logoUrl\": \"" + pickLogo(id) + "\" }";
        } else if (path.equals("/subscriptions") && method.equals("POST")) {
            mock = "{ \"id\": 999, \"serviceName\":\"신규구독\", \"amount\":10000, " +
                    "  \"startDate\":\"2025-08-12\", \"repeatCycleDays\":30, \"autoPayment\":true, \"isShared\":false }";
        } else if (path.matches("^/subscriptions/\\d+$") && method.equals("PUT")) {
            long id = extractId(path);
            mock = "{ \"id\": " + id + ", \"serviceName\":\"수정된구독\", \"amount\":13000, " +
                    "  \"startDate\":\"2025-08-20\", \"repeatCycleDays\":90, \"autoPayment\":false, \"isShared\":true }";
        } else if (path.matches("^/subscriptions/\\d+$") && method.equals("DELETE")) {
            mock = ""; code = HttpURLConnection.HTTP_NO_CONTENT;

            // ---- UPCOMING ----
        } else if (path.equals("/subscriptions/upcoming") && method.equals("GET")) {
            mock = "{ \"content\": [" +
                    "  {\"id\":10,\"serviceName\":\"넷플릭스\",\"amount\":13500,\"nextPaymentDate\":\"2025-09-12\"}," +
                    "  {\"id\":12,\"serviceName\":\"스포티파이\",\"amount\":7900,\"nextPaymentDate\":\"2025-09-03\"}" +
                    "], \"totalElements\":2, \"totalPages\":1 }";
        }

        return new Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(code == 204 ? "No Content" : "OK")
                .body(ResponseBody.create(mock, JSON))
                .build();
    }

    private static long extractId(String path) {
        Matcher m = Pattern.compile("/(\\d+)$").matcher(path);
        return m.find() ? Long.parseLong(m.group(1)) : 0L;
    }

    private static String pickName(long id) {
        if (id % 3 == 0) return "스포티파이";
        if (id % 2 == 0) return "디즈니+";
        return "넷플릭스";
    }

    private static int pickAmount(long id) {
        if (id % 3 == 0) return 7900;
        if (id % 2 == 0) return 10900;
        return 13500;
    }

    private static String pickLogo(long id) {
        if (id % 3 == 0) return "https://cdn/mock/spotify.png";
        if (id % 2 == 0) return "https://cdn/mock/disney.png";
        return "https://cdn/mock/netflix.png";
    }
}