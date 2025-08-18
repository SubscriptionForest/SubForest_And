package com.example.subforest.network;

import android.os.Build;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MockInterceptor implements Interceptor {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final Gson gson = new Gson();

    // ======= in-memory 데이터 =======
    private long nextId = 100;
    private final List<Map<String, Object>> subscriptions = new ArrayList<>();
    private final List<Map<String, Object>> services = new ArrayList<>();
    private final List<Map<String, Object>> customServices = new ArrayList<>();
    private final Map<String, Object> me = new LinkedHashMap<>();

    public MockInterceptor() {
        // 유저(마이페이지)
        me.put("id", 1);
        me.put("email", "test@naver.com");
        me.put("name", "홍길동");
        me.put("notificationEnabled", true);
        me.put("status", "ACTIVE");

        // 서비스 샘플(검색용)
        services.add(makeService(1, "넷플릭스", "https://cdn.example.com/netflix.png"));
        services.add(makeService(2, "유튜브 프리미엄", "https://cdn.example.com/youtube.png"));
        services.add(makeService(3, "디즈니+", "https://cdn.example.com/disney.png"));
        services.add(makeService(4, "왓챠", "https://cdn.example.com/watcha.png"));

        // 구독 샘플(리스트/상세/수정/삭제용)
        subscriptions.add(makeSub(10, "넷플릭스", 13500, "2025-08-12", 30, true, false, "https://cdn.example.com/netflix.png"));
        subscriptions.add(makeSub(11, "유튜브 프리미엄", 7900, "2025-08-01", 30, false, true, "https://cdn.example.com/youtube.png"));
        nextId = 12;
    }

    private Map<String, Object> makeService(long id, String name, @Nullable String logo) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("logoUrl", logo);
        return m;
    }

    private Map<String, Object> makeSub(long id, String serviceName, int amount, String startDate,
                                        int repeatDays, boolean auto, boolean shared, @Nullable String logo) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("serviceName", serviceName);
        m.put("amount", amount);
        m.put("startDate", startDate);
        m.put("repeatCycleDays", repeatDays);
        m.put("autoPayment", auto);
        m.put("isShared", shared);
        m.put("logoUrl", logo);
        return m;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request req = chain.request();
        String method = req.method();
        String path = req.url().encodedPath();            // "/subscriptions", "/mypage/me" 등
        String query = decode(req.url().encodedQuery());  // "page=0&size=20" 등
        String body = readBody(req);

        // ===== AUTH (대충만) =====
        if (method.equals("POST") && path.equals("/auth/login")) {
            String json = "{\"token\":\"mock-token\",\"email\":\"test@naver.com\",\"name\":\"홍길동\",\"id\":1}";
            return ok(req, json);
        }
        if (method.equals("POST") && path.equals("/auth/signup")) {
            // 그냥 성공했다고 가정
            String json = "{\"id\":2,\"email\":\"new@naver.com\",\"name\":\"새유저\"}";
            return ok(req, json);
        }
        if (method.equals("POST") && (path.equals("/auth/logout") || path.equals("/mypage/logout"))) {
            return ok(req, "{\"message\":\"Logged out successfully\"}");
        }

        // ===== MYPAGE =====
        if (method.equals("GET") && path.equals("/mypage/me")) {
            return ok(req, gson.toJson(me));
        }
        if (method.equals("POST") && path.equals("/mypage/change-password")) {
            return ok(req, "{\"message\":\"Password changed successfully\"}");
        }
        if (method.equals("PATCH") && path.equals("/mypage/notification")) {
            // enabled=true/false or body {notificationEnabled:true}
            Boolean enabled = null;
            String en = getQueryParam(query, "enabled");
            if (en != null) enabled = en.equalsIgnoreCase("true") || en.equals("1");
            if (enabled == null) {
                try {
                    Type t = new TypeToken<Map<String, Object>>(){}.getType();
                    Map<String, Object> map = gson.fromJson(body, t);
                    if (map != null && map.containsKey("notificationEnabled")) {
                        Object v = map.get("notificationEnabled");
                        if (v instanceof Boolean) enabled = (Boolean) v;
                        else if (v instanceof String) enabled = ((String) v).equalsIgnoreCase("true");
                    }
                } catch (Exception ignore) {}
            }
            if (enabled == null) enabled = true;
            me.put("notificationEnabled", enabled);
            return ok(req, "{\"notificationEnabled\":" + enabled + "}");
        }
        if (method.equals("POST") && path.equals("/mypage/deactivate")) {
            me.put("status", "INACTIVE");
            return ok(req, "{\"message\":\"Account deactivated\"}");
        }

        // ===== SERVICES =====
        if (method.equals("GET") && path.equals("/services/search")) {
            String q = getQueryParam(query, "q");
            List<Map<String, Object>> out = new ArrayList<>();
            if (q == null || q.trim().isEmpty()) {
                out.addAll(services);
            } else {
                String ql = q.toLowerCase(Locale.ROOT);
                for (Map<String, Object> s : services) {
                    String name = String.valueOf(s.get("name"));
                    if (name.toLowerCase(Locale.ROOT).contains(ql)) out.add(s);
                }
            }
            return ok(req, gson.toJson(out));
        }

        // ===== CUSTOM SERVICES =====
        if (method.equals("GET") && path.equals("/custom-services")) {
            return ok(req, gson.toJson(customServices));
        }
        if (method.equals("POST") && path.equals("/custom-services")) {
            // body: { userId, name, logoUrl }
            Type t = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> payload = safeJsonToMap(body, t);
            Map<String, Object> cs = new LinkedHashMap<>();
            cs.put("id", ++nextId);
            cs.put("userId", payload.getOrDefault("userId", 1));
            cs.put("name", payload.getOrDefault("name", "내서비스"));
            cs.put("logoUrl", payload.get("logoUrl"));
            customServices.add(cs);
            return ok(req, gson.toJson(cs));
        }

        // ===== SUBSCRIPTIONS =====
        if (method.equals("GET") && path.equals("/subscriptions")) {
            // Page<SubscriptionListItemDto>
            List<Map<String, Object>> content = new ArrayList<>();
            for (Map<String, Object> s : subscriptions) {
                Map<String, Object> lite = new LinkedHashMap<>();
                lite.put("id", s.get("id"));
                lite.put("serviceName", s.get("serviceName"));
                lite.put("amount", s.get("amount"));
                lite.put("nextPaymentDate", null);
                lite.put("logoUrl", s.get("logoUrl"));
                content.add(lite);
            }
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("content", content);
            res.put("totalElements", content.size());
            res.put("totalPages", 1);
            return ok(req, gson.toJson(res));
        }

        if (method.equals("GET") && path.startsWith("/subscriptions/")) {
            Long id = lastPathId(path);
            Map<String, Object> found = findById(subscriptions, id);
            if (found == null) return notFound(req);
            return ok(req, gson.toJson(found));
        }

        if (method.equals("POST") && path.equals("/subscriptions")) {
            // body: SubscriptionRequest (대충 받아서 저장)
            Type t = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> payload = safeJsonToMap(body, t);

            long id = ++nextId;
            String name = "커스텀";
            // serviceId / customServiceId 보고 이름 추정(간단히)
            Object sid = payload.get("serviceId");
            Object csid = payload.get("customServiceId");
            if (sid instanceof Number) {
                Map<String, Object> svc = findById(services, ((Number) sid).longValue());
                if (svc != null) name = String.valueOf(svc.get("name"));
            } else if (csid instanceof Number) {
                Map<String, Object> cs = findById(customServices, ((Number) csid).longValue());
                if (cs != null) name = String.valueOf(cs.get("name"));
            }

            Map<String, Object> sub = makeSub(
                    id,
                    name,
                    toInt(payload.get("amount"), 0),
                    str(payload.get("startDate"), "2025-08-01"),
                    toInt(payload.get("repeatCycleDays"), 30),
                    toBool(payload.get("autoPayment")),
                    toBool(payload.get("isShared")),
                    null
            );
            subscriptions.add(sub);
            return ok(req, gson.toJson(sub));
        }

        if (method.equals("PUT") && path.startsWith("/subscriptions/")) {
            Long id = lastPathId(path);
            Map<String, Object> target = findById(subscriptions, id);
            if (target == null) return notFound(req);

            Type t = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> payload = safeJsonToMap(body, t);

            if (payload.containsKey("amount")) target.put("amount", toInt(payload.get("amount"), (Integer) target.get("amount")));
            if (payload.containsKey("startDate")) target.put("startDate", str(payload.get("startDate"), (String) target.get("startDate")));
            if (payload.containsKey("repeatCycleDays")) target.put("repeatCycleDays", toInt(payload.get("repeatCycleDays"), (Integer) target.get("repeatCycleDays")));
            if (payload.containsKey("autoPayment")) target.put("autoPayment", toBool(payload.get("autoPayment")));
            if (payload.containsKey("isShared")) target.put("isShared", toBool(payload.get("isShared")));

            // 서비스/커스텀 서비스 변경 시 이름 교체(간단히)
            if (payload.containsKey("serviceId")) {
                Map<String, Object> svc = findById(services, toLong(payload.get("serviceId"), -1));
                if (svc != null) {
                    target.put("serviceName", svc.get("name"));
                    target.put("logoUrl", svc.get("logoUrl"));
                }
            } else if (payload.containsKey("customServiceId")) {
                Map<String, Object> cs = findById(customServices, toLong(payload.get("customServiceId"), -1));
                if (cs != null) {
                    target.put("serviceName", cs.get("name"));
                    target.put("logoUrl", cs.get("logoUrl"));
                }
            }
            return ok(req, gson.toJson(target));
        }

        if (method.equals("DELETE") && path.startsWith("/subscriptions/")) {
            Long id = lastPathId(path);
            Map<String, Object> found = findById(subscriptions, id);
            if (found == null) return notFound(req);
            subscriptions.remove(found);
            return noContent(req);
        }

        if (method.equals("GET") && path.equals("/subscriptions/upcoming")) {
            // 간단히 전체 반환
            List<Map<String, Object>> content = new ArrayList<>();
            for (Map<String, Object> s : subscriptions) {
                Map<String, Object> lite = new LinkedHashMap<>();
                lite.put("id", s.get("id"));
                lite.put("serviceName", s.get("serviceName"));
                lite.put("amount", s.get("amount"));
                lite.put("nextPaymentDate", null);
                lite.put("logoUrl", s.get("logoUrl"));
                content.add(lite);
            }
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("content", content);
            res.put("totalElements", content.size());
            res.put("totalPages", 1);
            return ok(req, gson.toJson(res));
        }

        // 모르는 요청은 실제 네트워크로
        return chain.proceed(req);
    }

    // ===== helpers =====

    private Response ok(Request req, String json) {
        return new Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200).message("OK")
                .body(ResponseBody.create(json, JSON))
                .build();
    }

    private Response noContent(Request req) {
        return new Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(204).message("No Content")
                .build();
    }

    private Response notFound(Request req) {
        return new Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(404).message("Not Found")
                .body(ResponseBody.create("{\"message\":\"not found\"}", JSON))
                .build();
    }

    private String readBody(Request req) throws IOException {
        if (req.body() == null) return "";
        okio.Buffer buf = new okio.Buffer();
        req.body().writeTo(buf);
        return buf.readUtf8();
    }

    private static String decode(@Nullable String s) {
        if (s == null) return "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        }
        return null;
    }

    @Nullable
    private String getQueryParam(@Nullable String query, String key) {
        if (query == null || query.isEmpty()) return null;
        String[] pairs = query.split("&");
        for (String p : pairs) {
            int i = p.indexOf('=');
            if (i <= 0) continue;
            String k = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                k = URLDecoder.decode(p.substring(0, i), StandardCharsets.UTF_8);
            }
            if (k.equals(key)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return URLDecoder.decode(p.substring(i + 1), StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }

    private Long lastPathId(String path) {
        try {
            String[] parts = path.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            return -1L;
        }
    }

    private Map<String, Object> findById(List<Map<String, Object>> list, long id) {
        for (Map<String, Object> m : list) {
            Object v = m.get("id");
            if (v instanceof Number && ((Number) v).longValue() == id) return m;
        }
        return null;
    }

    private Map<String, Object> safeJsonToMap(String json, Type type) {
        try { return gson.fromJson(json, type); } catch (Exception e) { return new LinkedHashMap<>(); }
    }

    private int toInt(Object o, int def) {
        try {
            if (o instanceof Number) return ((Number) o).intValue();
            if (o instanceof String) return Integer.parseInt((String) o);
        } catch (Exception ignore) {}
        return def;
    }

    private long toLong(Object o, long def) {
        try {
            if (o instanceof Number) return ((Number) o).longValue();
            if (o instanceof String) return Long.parseLong((String) o);
        } catch (Exception ignore) {}
        return def;
    }

    private boolean toBool(Object o) {
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof String) return ((String) o).equalsIgnoreCase("true") || o.equals("1");
        return false;
    }

    private String str(Object o, String def) {
        return o == null ? def : String.valueOf(o);
    }
}