package com.example.subforest.network;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import com.example.subforest.network.ApiDtos.*;
import com.example.subforest.ui.ImageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiRepository {

    // ===== UI에 노출할 모델 =====
    public static class ServiceItem { public long id; public String name; public String logoUrl; }
    public static class CustomServiceItem { public long id; public String name; public String logoUrl; }
    public static class SubscriptionItem {
        public long id; public String name; public int amount;
        public String startDate; public int repeatDays;
        public boolean autoPayment; public boolean shared;
        @Nullable public String logoUrl;
    }
    public static class UserProfile {
        public long id; public String email; public String name; public boolean notificationEnabled;
    }

    // ===== 콜백 =====
    public interface RepoCallback<T> { void onSuccess(T data); void onError(String message); }

    // ===== 싱글턴 =====
    private static volatile ApiRepository INSTANCE;
    public static ApiRepository get(Context ctx) {
        if (INSTANCE == null) {
            synchronized (ApiRepository.class) {
                if (INSTANCE == null) INSTANCE = new ApiRepository(ctx.getApplicationContext());
            }
        }
        return INSTANCE;
    }

    private final Context app;
    private final Context appContext;
    private final ApiService api;
    private final TokenStore tokenStore;
    private final Handler main = new Handler(Looper.getMainLooper());

    private ApiRepository(Context app) {
        this.appContext = app.getApplicationContext();
        this.app = app;
        this.api = ApiClient.service(app);
        this.tokenStore = new TokenStore(app);
    }

    private long uid() { return tokenStore.getUserIdOr(1L); }
    private void postErr(RepoCallback<?> cb, String msg) { main.post(() -> cb.onError(msg)); }

    // ===== 서비스 검색 + 커스텀 병합 =====
    public void getServices(@Nullable String query, RepoCallback<List<ServiceItem>> cb) {
        api.searchServices(query == null ? "" : query).enqueue(new Callback<List<ApiDtos.ServiceItem>>() {
            @Override public void onResponse(Call<List<ApiDtos.ServiceItem>> call, Response<List<ApiDtos.ServiceItem>> resp) {
                if (!resp.isSuccessful() || resp.body()==null) { postErr(cb, "서비스 검색 실패(" + resp.code() + ")"); return; }
                List<ServiceItem> merged = new ArrayList<>();
                for (ApiDtos.ServiceItem s : resp.body()) {
                    ServiceItem x = new ServiceItem();
                    x.id = s.id; x.name = s.name; x.logoUrl = s.logoUrl;
                    merged.add(x);
                }
                api.getCustomServices(uid()).enqueue(new Callback<List<ApiDtos.CustomServiceItem>>() {
                    @Override public void onResponse(Call<List<ApiDtos.CustomServiceItem>> call2, Response<List<ApiDtos.CustomServiceItem>> resp2) {
                        if (resp2.isSuccessful() && resp2.body()!=null) {
                            for (ApiDtos.CustomServiceItem cs : resp2.body()) {
                                ServiceItem x = new ServiceItem();
                                x.id = cs.id; x.name = cs.name; x.logoUrl = cs.logoUrl;
                                merged.add(x);
                            }
                        }
                        main.post(() -> cb.onSuccess(merged));
                    }
                    @Override public void onFailure(Call<List<ApiDtos.CustomServiceItem>> call2, Throwable t) {
                        main.post(() -> cb.onSuccess(merged)); // 커스텀 실패해도 기본 반환
                    }
                });
            }
            @Override public void onFailure(Call<List<ApiDtos.ServiceItem>> call, Throwable t) { postErr(cb, t.getMessage()); }
        });
    }

    // ===== 커스텀 서비스 등록 (이미지 업로드 없이 문자열/URI 전송 허용) =====
    public void createCustomService(String name, @Nullable Uri imageUri, RepoCallback<CustomServiceItem> cb) {
        CustomServiceCreate body = new CustomServiceCreate();
        body.userId = uid();
        body.name = name;

        // 내부 저장 후 DB 경로 세팅
        String dbPath = ImageUtils.saveLogoAndReturnDbUrl(appContext, imageUri, name);
        body.logoUrl = dbPath; // null 가능(이미지 미선택)

        api.createCustomService(body).enqueue(new Callback<ApiDtos.CustomServiceItem>() {
            @Override public void onResponse(Call<ApiDtos.CustomServiceItem> call, Response<ApiDtos.CustomServiceItem> resp) {
                if (resp.isSuccessful() && resp.body()!=null) {
                    CustomServiceItem out = new CustomServiceItem();
                    out.id = resp.body().id; out.name = resp.body().name; out.logoUrl = resp.body().logoUrl;
                    main.post(() -> cb.onSuccess(out));
                } else postErr(cb, "커스텀 서비스 등록 실패(" + resp.code() + ")");
            }
            @Override public void onFailure(Call<ApiDtos.CustomServiceItem> call, Throwable t) { postErr(cb, t.getMessage()); }
        });
    }

    // ===== 구독 생성/수정/삭제 =====
    public void createSubscription(@Nullable Long serviceId, @Nullable Long customServiceId,
                                   int amount, String startYmd, int repeatDays,
                                   boolean auto, boolean shared,
                                   RepoCallback<SubscriptionItem> cb) {
        SubscriptionRequest b = new SubscriptionRequest();
        b.userId = uid(); b.serviceId = serviceId; b.customServiceId = customServiceId;
        b.amount = amount; b.startDate = startYmd; b.repeatCycleDays = repeatDays; b.autoPayment = auto; b.isShared = shared;

        api.createSubscription(b).enqueue(new Callback<SubscriptionResponse>() {
            @Override public void onResponse(Call<SubscriptionResponse> call, Response<SubscriptionResponse> r) {
                if (!r.isSuccessful() || r.body()==null) { postErr(cb, "구독 등록 실패(" + r.code() + ")"); return; }
                main.post(() -> cb.onSuccess(map(r.body())));
            }
            @Override public void onFailure(Call<SubscriptionResponse> call, Throwable t) { postErr(cb, t.getMessage()); }
        });
    }

    public void updateSubscription(long id, @Nullable Long serviceId, @Nullable Long customServiceId,
                                   int amount, String startYmd, int repeatDays,
                                   boolean auto, boolean shared,
                                   RepoCallback<SubscriptionItem> cb) {
        SubscriptionRequest b = new SubscriptionRequest();
        b.userId = uid(); b.serviceId = serviceId; b.customServiceId = customServiceId;
        b.amount = amount; b.startDate = startYmd; b.repeatCycleDays = repeatDays; b.autoPayment = auto; b.isShared = shared;

        api.updateSubscription(id, b).enqueue(new Callback<SubscriptionResponse>() {
            @Override public void onResponse(Call<SubscriptionResponse> call, Response<SubscriptionResponse> r) {
                if (!r.isSuccessful() || r.body()==null) { postErr(cb, "구독 수정 실패(" + r.code() + ")"); return; }
                main.post(() -> cb.onSuccess(map(r.body())));
            }
            @Override public void onFailure(Call<SubscriptionResponse> call, Throwable t) { postErr(cb, t.getMessage()); }
        });
    }

    public void deleteSubscription(long id, RepoCallback<Boolean> cb) {
        api.deleteSubscription(id).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> r) {
                if (r.isSuccessful()) main.post(() -> cb.onSuccess(true));
                else postErr(cb, "삭제 실패(" + r.code() + ")");
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { postErr(cb, t.getMessage()); }
        });
    }

    /** 목록 → 상세 보강해서 UI모델 구성 */
    public void getSubscriptions(RepoCallback<List<SubscriptionItem>> cb) {
        api.getSubscriptions(uid(), 0, 100).enqueue(new Callback<PagedList<SubscriptionListItemDto>>() {
            @Override public void onResponse(Call<PagedList<SubscriptionListItemDto>> call, Response<PagedList<SubscriptionListItemDto>> r) {
                if (!r.isSuccessful() || r.body()==null || r.body().content==null) { postErr(cb, "목록 실패(" + r.code() + ")"); return; }
                List<SubscriptionListItemDto> base = r.body().content;
                if (base.isEmpty()) { main.post(() -> cb.onSuccess(new ArrayList<>())); return; }

                List<SubscriptionItem> out = Collections.synchronizedList(new ArrayList<>());
                AtomicInteger left = new AtomicInteger(base.size());

                for (SubscriptionListItemDto lite : base) {
                    api.getSubscription(lite.id).enqueue(new Callback<SubscriptionResponse>() {
                        @Override public void onResponse(Call<SubscriptionResponse> call2, Response<SubscriptionResponse> d) {
                            if (d.isSuccessful() && d.body()!=null) {
                                SubscriptionItem m = map(d.body());
                                if (m.amount == 0 && lite.amount != null) m.amount = lite.amount;
                                if (m.logoUrl == null) m.logoUrl = lite.logoUrl;
                                out.add(m);
                            }
                            if (left.decrementAndGet()==0) main.post(() -> cb.onSuccess(new ArrayList<>(out)));
                        }
                        @Override public void onFailure(Call<SubscriptionResponse> call2, Throwable t) {
                            if (left.decrementAndGet()==0) main.post(() -> cb.onSuccess(new ArrayList<>(out)));
                        }
                    });
                }
            }
            @Override public void onFailure(Call<PagedList<SubscriptionListItemDto>> call, Throwable t) { postErr(cb, t.getMessage()); }
        });
    }

    private static SubscriptionItem map(SubscriptionResponse r) {
        SubscriptionItem s = new SubscriptionItem();
        s.id = r.id;
        s.name = r.serviceName != null ? r.serviceName : "";
        s.amount = r.amount != null ? r.amount : 0;
        s.startDate = r.startDate != null ? r.startDate : "1970-01-01";
        s.repeatDays = r.repeatCycleDays != null ? r.repeatCycleDays : 30;
        s.autoPayment = r.autoPayment != null && r.autoPayment;
        s.shared = r.isShared != null && r.isShared;
        s.logoUrl = r.logoUrl;
        return s;
    }

    // ===== 마이페이지 =====
    public void getMe(RepoCallback<UserProfile> cb) {
        api.getMe().enqueue(new Callback<ApiDtos.UserProfile>() {
            @Override public void onResponse(Call<ApiDtos.UserProfile> call, Response<ApiDtos.UserProfile> resp) {
                if (resp.isSuccessful() && resp.body()!=null) {
                    tokenStore.saveUserId(resp.body().id);
                    UserProfile u = new UserProfile();
                    u.id = resp.body().id; u.email = resp.body().email; u.name = resp.body().name;
                    u.notificationEnabled = resp.body().notificationEnabled;
                    main.post(() -> cb.onSuccess(u));
                } else postErr(cb, "내 정보 실패(" + resp.code() + ")");
            }
            @Override public void onFailure(Call<ApiDtos.UserProfile> call, Throwable t) { postErr(cb, t.getMessage()); }
        });
    }

    public void updateNotification(boolean enabled, RepoCallback<UserProfile> cb) {
        api.updateNotification(new NotificationToggleReq(enabled)).enqueue(new Callback<NotificationToggleRes>() {
            @Override public void onResponse(Call<NotificationToggleRes> call, Response<NotificationToggleRes> resp) {
                if (!resp.isSuccessful()) { postErr(cb, "알림 설정 실패(" + resp.code() + ")"); return; }
                // 전체 프로필을 다시 불러 동일 시그니처 반환
                getMe(cb);
            }
            @Override public void onFailure(Call<NotificationToggleRes> call, Throwable t) { postErr(cb, t.getMessage()); }
        });
    }

    public void changePassword(String current, String newPw, RepoCallback<Boolean> cb) {
        api.changePassword(new ApiDtos.ChangePasswordReq(current, newPw)).enqueue(new Callback<MessageResponse>() {
            @Override public void onResponse(Call<MessageResponse> call, Response<MessageResponse> resp) {
                if (resp.isSuccessful()) main.post(() -> cb.onSuccess(true));
                else postErr(cb, "비번 변경 실패(" + resp.code() + ")");
            }
            @Override public void onFailure(Call<MessageResponse> call, Throwable t) { postErr(cb, t.getMessage()); }
        });
    }

    public void deleteAccount(RepoCallback<Boolean> cb) {
        api.deactivate().enqueue(new Callback<MessageResponse>() {
            @Override public void onResponse(Call<MessageResponse> call, Response<MessageResponse> resp) {
                if (resp.isSuccessful()) main.post(() -> cb.onSuccess(true));
                else postErr(cb, "비활성화 실패(" + resp.code() + ")");
            }
            @Override public void onFailure(Call<MessageResponse> call, Throwable t) { postErr(cb, t.getMessage()); }
        });
    }

    public void registerFcmToken(String fcmToken, RepoCallback<Boolean> cb) {
        Map<String, String> body = new HashMap<>();
        body.put("fcmToken", fcmToken);
        api.registerFcm(body).enqueue(new retrofit2.Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> resp) {
                if (resp.isSuccessful()) main.post(() -> cb.onSuccess(true));
                else postErr(cb, "FCM 등록 실패(" + resp.code() + ")");
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { postErr(cb, t.getMessage()); }
        });
    }

    public void togglePush(boolean enabled, RepoCallback<Boolean> cb) {
        api.togglePush(enabled).enqueue(new retrofit2.Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> resp) {
                if (resp.isSuccessful()) main.post(() -> cb.onSuccess(true));
                else postErr(cb, "알림 설정 실패(" + resp.code() + ")");
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { postErr(cb, t.getMessage()); }
        });
    }
}