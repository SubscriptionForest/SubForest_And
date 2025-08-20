package com.example.subforest.network;

import android.os.Build;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 가벼운 메모리 기반 목 서버
 * - 로그인/회원가입
 * - 마이페이지(me/notification/deactivate/logout)
 * - 서비스 검색 / 커스텀 서비스 GET/POST
 * - 구독 목록/상세/등록/수정/삭제
 * - 대시보드 요약 / 임박 목록
 */
public class MockInterceptor implements Interceptor {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final Gson gson = new Gson();

    private long nextId = 100;
    private final List<Map<String, Object>> subs = new ArrayList<>();
    private final List<Map<String, Object>> services = new ArrayList<>();
    private final List<Map<String, Object>> customs = new ArrayList<>();
    private final Map<String, Object> me = new LinkedHashMap<>();

    public MockInterceptor() {
        // me
        me.put("id", 1);
        me.put("email", "test@naver.com");
        me.put("name", "홍길동");
        me.put("notificationEnabled", true);
        me.put("status", "ACTIVE");

        // services
        services.add(svc(1, "넷플릭스", "/static/logo/netflix.png"));
        services.add(svc(2, "유튜브 프리미엄", "/static/logo/youtube.png"));
        services.add(svc(3, "디즈니+", "/static/logo/disney.png"));
        services.add(svc(4, "왓챠", "/static/logo/watcha.png"));

        // subscriptions (샘플)
        subs.add(sub(10, "넷플릭스", 13500, "2025-08-12", 30, true, false, "/static/logo/netflix.png"));
        subs.add(sub(11, "유튜브 프리미엄", 7900, "2025-08-01", 30, false, true, "/static/logo/youtube.png"));
        nextId = 12;
    }

    private Map<String, Object> svc(long id, String name, @Nullable String logo) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("name", name); m.put("logoUrl", logo);
        return m;
    }

    private Map<String, Object> sub(long id, String serviceName, int amount, String startDate,
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
        String path   = req.url().encodedPath();              // "/subscriptions" 형태
        String query  = dec(req.url().encodedQuery());
        String body   = readBody(req);

        // -------- Auth --------
        if (is(method, "POST") && eq(path, "/auth/login"))  return ok(req, "{\"token\":\"mock-token\",\"email\":\"test@naver.com\",\"name\":\"홍길동\"}");
        if (is(method, "POST") && eq(path, "/auth/signup")) return ok(req, "{\"id\":2,\"email\":\"new@naver.com\",\"name\":\"새유저\"}");
        if (is(method, "POST") && eq(path, "/auth/logout")) return ok(req, msg("Logged out successfully"));

        // -------- MyPage --------
        if (is(method, "GET")  && eq(path, "/mypage/me"))               return ok(req, gson.toJson(me));
        if (is(method, "POST") && eq(path, "/mypage/change-password"))  return ok(req, msg("Password changed successfully"));
        if (is(method, "PATCH")&& eq(path, "/mypage/notification")) {
            Boolean enabled = qpBool(query, "enabled");
            if (enabled == null) enabled = bodyBool(body, "notificationEnabled", true);
            me.put("notificationEnabled", enabled);
            return ok(req, "{\"notificationEnabled\":" + enabled + "}");
        }
        if (is(method, "POST") && eq(path, "/mypage/deactivate")) { me.put("status","INACTIVE"); return ok(req, msg("Account deactivated")); }
        if (is(method, "POST") && eq(path, "/mypage/logout"))     return ok(req, msg("Logged out successfully"));

        // -------- Dashboard --------
        if (is(method, "GET") && eq(path, "/dashboard/summary")) {
            int total = subs.stream().mapToInt(s -> toInt(s.get("amount"), 0)).sum();
            Map<String, Integer> chart = new LinkedHashMap<>();
            for (Map<String,Object> s : subs) {
                String n = String.valueOf(s.get("serviceName"));
                int a = toInt(s.get("amount"), 0);
                chart.put(n, chart.getOrDefault(n, 0) + a);
            }
            Map<String,Object> res = new LinkedHashMap<>();
            res.put("totalAmount", total);
            res.put("subscriptionCount", subs.size());
            res.put("chartData", chart);
            return ok(req, gson.toJson(res));
        }

        // -------- Services --------
        if (is(method, "GET") && eq(path, "/services/search")) {
            String q = qp(query, "q");
            List<Map<String, Object>> out = new ArrayList<>();
            if (q == null || q.trim().isEmpty()) out.addAll(services);
            else {
                String ql = q.toLowerCase(Locale.ROOT);
                for (Map<String, Object> s : services) {
                    if (String.valueOf(s.get("name")).toLowerCase(Locale.ROOT).contains(ql)) out.add(s);
                }
            }
            return ok(req, gson.toJson(out));
        }

        // -------- Custom Services --------
        if (is(method, "GET") && eq(path, "/custom-services"))  return ok(req, gson.toJson(customs));
        if (is(method, "POST")&& eq(path, "/custom-services")) {
            Map<String, Object> p = jsonToMap(body);
            Map<String, Object> cs = new LinkedHashMap<>();
            cs.put("id", ++nextId);
            cs.put("userId", p.getOrDefault("userId", 1));
            cs.put("name", p.getOrDefault("name", "내서비스"));
            cs.put("logoUrl", p.get("logoUrl"));
            customs.add(cs);
            return ok(req, gson.toJson(cs));
        }

        // -------- Subscriptions --------
        // 목록 (pageable)
        if (is(method, "GET") && eq(path, "/subscriptions")) {
            List<Map<String, Object>> content = new ArrayList<>();
            for (Map<String, Object> s : subs) {
                Map<String, Object> lite = new LinkedHashMap<>();
                lite.put("id", s.get("id"));
                lite.put("serviceName", s.get("serviceName"));
                lite.put("amount", s.get("amount"));
                lite.put("nextPaymentDate", null);
                lite.put("logoUrl", s.get("logoUrl"));
                content.add(lite);
            }
            Map<String, Object> page = new LinkedHashMap<>();
            page.put("content", content);
            page.put("totalElements", content.size());
            page.put("totalPages", 1);
            return ok(req, gson.toJson(page));
        }

        // 상세
        if (is(method, "GET") && path.matches("^/subscriptions/\\d+$")) {
            long id = lastId(path);
            Map<String, Object> f = findById(subs, id);
            if (f == null) return notFound(req);
            return ok(req, gson.toJson(f));
        }

        // 등록
        if (is(method, "POST") && eq(path, "/subscriptions")) {
            Map<String, Object> p = jsonToMap(body);
            long id = ++nextId;
            String name = "커스텀";
            Object sid = p.get("serviceId"), csid = p.get("customServiceId");
            if (sid instanceof Number) {
                Map<String,Object> s = findById(services, ((Number) sid).longValue());
                if (s != null) name = String.valueOf(s.get("name"));
            } else if (csid instanceof Number) {
                Map<String,Object> c = findById(customs, ((Number) csid).longValue());
                if (c != null) name = String.valueOf(c.get("name"));
            }
            Map<String, Object> m = sub(
                    id,
                    name,
                    toInt(p.get("amount"), 0),
                    str(p.get("startDate"), "2025-08-01"),
                    toInt(p.get("repeatCycleDays"), 30),
                    toBool(p.get("autoPayment")),
                    toBool(p.get("isShared")),
                    null
            );
            subs.add(m);
            return ok(req, gson.toJson(m));
        }

        // 수정
        if (is(method, "PUT") && path.matches("^/subscriptions/\\d+$")) {
            long id = lastId(path);
            Map<String, Object> t = findById(subs, id);
            if (t == null) return notFound(req);
            Map<String,Object> p = jsonToMap(body);
            if (p.containsKey("amount"))          t.put("amount", toInt(p.get("amount"), toInt(t.get("amount"),0)));
            if (p.containsKey("startDate"))       t.put("startDate", str(p.get("startDate"), (String)t.get("startDate")));
            if (p.containsKey("repeatCycleDays")) t.put("repeatCycleDays", toInt(p.get("repeatCycleDays"), toInt(t.get("repeatCycleDays"),30)));
            if (p.containsKey("autoPayment"))     t.put("autoPayment", toBool(p.get("autoPayment")));
            if (p.containsKey("isShared"))        t.put("isShared", toBool(p.get("isShared")));
            if (p.containsKey("serviceId")) {
                Map<String,Object> s = findById(services, toLong(p.get("serviceId"), -1));
                if (s != null) { t.put("serviceName", s.get("name")); t.put("logoUrl", s.get("logoUrl")); }
            }
            if (p.containsKey("customServiceId")) {
                Map<String,Object> c = findById(customs, toLong(p.get("customServiceId"), -1));
                if (c != null) { t.put("serviceName", c.get("name")); t.put("logoUrl", c.get("logoUrl")); }
            }
            return ok(req, gson.toJson(t));
        }

        // 삭제
        if (is(method, "DELETE") && path.matches("^/subscriptions/\\d+$")) {
            long id = lastId(path);
            Map<String, Object> f = findById(subs, id);
            if (f == null) return notFound(req);
            subs.remove(f);
            return noContent(req);
        }

        // 임박 목록
        if (is(method, "GET") && eq(path, "/subscriptions/upcoming")) {
            List<Map<String, Object>> ups = new ArrayList<>();
            for (Map<String, Object> s : subs) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("serviceName", s.get("serviceName"));
                m.put("amount", s.get("amount"));
                m.put("nextPaymentDate", null);
                m.put("logoUrl", s.get("logoUrl"));
                ups.add(m);
            }
            return ok(req, gson.toJson(Collections.singletonMap("content", ups)));
        }

        // POST /api/push/register
        if (is(method, "POST") && eq(path, "/api/push/register")) {
            // body: {"fcmToken":"..."} → 저장만 흉내
            return ok(req, "{}"); // 200 본문 없음 대응
        }

// POST /api/push/toggle?enabled=true|false
        if (is(method, "POST") && eq(path, "/api/push/toggle")) {
            Boolean enabled = qpBool(query, "enabled");
            // me.put("notificationEnabled", enabled); // 원하면 내부 상태 업데이트
            return ok(req, "{}");
        }

        // 알 수 없는 건 실제 통신
        return chain.proceed(req);
    }

    // ---------- helpers ----------
    private boolean is(String m, String target) { return m.equalsIgnoreCase(target); }
    private boolean eq(String a, String b) { return a.equals(b); }

    private Response ok(Request req, String json) {
        return new Response.Builder()
                .request(req).protocol(Protocol.HTTP_1_1)
                .code(200).message("OK")
                .body(ResponseBody.create(json, JSON))
                .build();
    }
    private Response noContent(Request req) {
        return new Response.Builder()
                .request(req).protocol(Protocol.HTTP_1_1)
                .code(204).message("No Content")
                .build();
    }
    private Response notFound(Request req) {
        return new Response.Builder()
                .request(req).protocol(Protocol.HTTP_1_1)
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
    private static String dec(@Nullable String s) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return (s == null) ? "" : URLDecoder.decode(s, StandardCharsets.UTF_8);
        }
        return null;
    }
    @Nullable
    private String qp(@Nullable String query, String key) {
        if (query == null || query.isEmpty()) return null;
        for (String p : query.split("&")) {
            int i = p.indexOf('=');
            if (i <= 0) continue;
            String k = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                k = URLDecoder.decode(p.substring(0, i), StandardCharsets.UTF_8);
            }
            if (k.equals(key)) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return URLDecoder.decode(p.substring(i + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }
    private Boolean qpBool(String query, String key) {
        String v = qp(query, key);
        if (v == null) return null;
        return "true".equalsIgnoreCase(v) || "1".equals(v);
    }
    private Boolean bodyBool(String body, String key, boolean def) {
        try {
            Type t = new TypeToken<Map<String,Object>>(){}.getType();
            Map<String,Object> m = gson.fromJson(body, t);
            Object v = m.get(key);
            if (v instanceof Boolean) return (Boolean) v;
            if (v instanceof String) return "true".equalsIgnoreCase((String)v) || "1".equals(v);
        } catch (Exception ignore) {}
        return def;
    }
    private long lastId(String path) {
        try { String[] p = path.split("/"); return Long.parseLong(p[p.length - 1]); }
        catch (Exception e) { return -1; }
    }
    private Map<String, Object> findById(List<Map<String, Object>> list, long id) {
        for (Map<String, Object> m : list) {
            Object v = m.get("id");
            if (v instanceof Number && ((Number) v).longValue() == id) return m;
        }
        return null;
    }
    private Map<String, Object> jsonToMap(String json) {
        try {
            Type t = new TypeToken<Map<String,Object>>(){}.getType();
            Map<String,Object> m = gson.fromJson(json, t);
            return m != null ? m : new LinkedHashMap<>();
        } catch (Exception e) { return new LinkedHashMap<>(); }
    }
    private String msg(String m) { return "{\"message\":\"" + m + "\"}"; }
    private int toInt(Object o, int def) {
        try { if (o instanceof Number) return ((Number) o).intValue(); return Integer.parseInt(String.valueOf(o)); }
        catch (Exception e) { return def; }
    }
    private long toLong(Object o, long def) {
        try { if (o instanceof Number) return ((Number) o).longValue(); return Long.parseLong(String.valueOf(o)); }
        catch (Exception e) { return def; }
    }
    private boolean toBool(Object o) {
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof String) return "true".equalsIgnoreCase((String) o) || "1".equals(o);
        return false;
    }
    private String str(Object o, String def) { return o == null ? def : String.valueOf(o); }
}