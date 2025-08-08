package com.example.subforest.network;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class ApiRepository {

    public interface RepoCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    // ===== 공개 ViewModel =====
    public static final class AuthResult {
        public final String token;
        public final UserProfile user;
        AuthResult(String token, UserProfile user){ this.token=token; this.user=user; }
    }
    public static final class UserProfile {
        public final long id;
        public final String name, email, status;
        public final boolean emailVerified, notificationEnabled;
        UserProfile(long id, String name, String email, boolean emailVerified, String status, boolean notificationEnabled){
            this.id=id; this.name=name; this.email=email; this.emailVerified=emailVerified; this.status=status; this.notificationEnabled=notificationEnabled;
        }
    }
    public static final class ServiceItem {
        public final long id;
        public final String name, logoUrl;
        ServiceItem(long id, String name, String logoUrl){ this.id=id; this.name=name; this.logoUrl=logoUrl; }
        @Override public String toString(){ return name; } // Spinner 표시
    }
    public static final class CustomServiceItem {
        public final long id;
        public final String name, logoUrl;
        CustomServiceItem(long id, String name, String logoUrl){ this.id=id; this.name=name; this.logoUrl=logoUrl; }
    }
    public static final class SubscriptionItem {
        public final long id;
        public final String name, logoUrl;
        public final int amount;
        public final String startDate;
        public final int repeatDays;
        public final boolean autoPayment, shared;
        SubscriptionItem(long id, String name, String logoUrl, int amount, String startDate, int repeatDays, boolean autoPayment, boolean shared){
            this.id=id; this.name=name; this.logoUrl=logoUrl; this.amount=amount; this.startDate=startDate; this.repeatDays=repeatDays; this.autoPayment=autoPayment; this.shared=shared;
        }
    }

    // ===== 싱글턴 =====
    private static ApiRepository instance;
    private final ApiService api;
    private final Context app;

    private ApiRepository(Context context){
        this.app = context.getApplicationContext();
        this.api = ApiClient.get(app).create(ApiService.class);
    }
    public static ApiRepository get(Context context){
        if (instance==null) instance = new ApiRepository(context);
        return instance;
    }

    // ===== Auth =====
    public void login(String email, String password, RepoCallback<AuthResult> cb){
        enqueue(api.login(new LoginRequest(email, password)), new RepoCallback<AuthResponse>() {
            @Override public void onSuccess(AuthResponse d) { cb.onSuccess(new AuthResult(d.token, mapUser(d.user))); }
            @Override public void onError(String m) { cb.onError(m); }
        });
    }
    public void register(String name, String email, String password, RepoCallback<AuthResult> cb){
        enqueue(api.register(new RegisterRequest(name, email, password)), new RepoCallback<AuthResponse>() {
            @Override public void onSuccess(AuthResponse d) { cb.onSuccess(new AuthResult(d.token, mapUser(d.user))); }
            @Override public void onError(String m) { cb.onError(m); }
        });
    }

    // ===== Services =====
    public void getServices(@Nullable String keyword, RepoCallback<List<ServiceItem>> cb){
        enqueue(api.getServices(keyword), new RepoCallback<List<ServiceDto>>() {
            @Override public void onSuccess(List<ServiceDto> list) {
                ArrayList<ServiceItem> out = new ArrayList<>();
                for (ServiceDto d : list) out.add(new ServiceItem(d.id, d.name, d.logo_url));
                cb.onSuccess(out);
            }
            @Override public void onError(String m) { cb.onError(m); }
        });
    }
    public void createCustomService(String name, @Nullable Uri logoUri, RepoCallback<CustomServiceItem> cb){
        RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), name);
        MultipartBody.Part logoPart = (logoUri == null) ? null : toImagePart(logoUri, "logo");
        enqueue(api.createCustomService(nameBody, logoPart), new RepoCallback<CustomServiceDto>() {
            @Override public void onSuccess(CustomServiceDto d) { cb.onSuccess(new CustomServiceItem(d.id, d.name, d.logo_url)); }
            @Override public void onError(String m) { cb.onError(m); }
        });
    }

    // ===== Subscriptions =====
    public void getSubscriptions(RepoCallback<List<SubscriptionItem>> cb){
        enqueue(api.getSubscriptions(), new RepoCallback<List<SubscriptionDto>>() {
            @Override public void onSuccess(List<SubscriptionDto> list) {
                ArrayList<SubscriptionItem> out = new ArrayList<>();
                for (SubscriptionDto d : list) {
                    out.add(new SubscriptionItem(
                            d.id,
                            d.service_name != null ? d.service_name : "(이름 없음)",
                            d.logo_url,
                            d.amount,
                            d.start_date,
                            d.repeat_cycle_days,
                            d.auto_payment,
                            d.is_shared
                    ));
                }
                cb.onSuccess(out);
            }
            @Override public void onError(String m) { cb.onError(m); }
        });
    }
    public void createSubscription(@Nullable Long serviceId, @Nullable Long customServiceId,
                                   int amount, String startDate, int repeatDays,
                                   boolean autoPayment, boolean shared,
                                   RepoCallback<SubscriptionItem> cb){
        CreateSubscriptionRequest req = new CreateSubscriptionRequest(serviceId, customServiceId, amount, startDate, repeatDays, autoPayment, shared);
        enqueue(api.createSubscription(req), new RepoCallback<SubscriptionDto>() {
            @Override public void onSuccess(SubscriptionDto d) {
                cb.onSuccess(new SubscriptionItem(
                        d.id, d.service_name != null ? d.service_name : "(이름 없음)",
                        d.logo_url, d.amount, d.start_date, d.repeat_cycle_days, d.auto_payment, d.is_shared
                ));
            }
            @Override public void onError(String m) { cb.onError(m); }
        });
    }
    public void updateSubscription(long id,
                                   @Nullable Long serviceId, @Nullable Long customServiceId,
                                   int amount, String startDate, int repeatDays,
                                   boolean autoPayment, boolean shared,
                                   RepoCallback<SubscriptionItem> cb) {
        UpdateSubscriptionRequest req = new UpdateSubscriptionRequest(
                serviceId, customServiceId, amount, startDate, repeatDays, autoPayment, shared
        );
        enqueue(api.updateSubscription(id, req), new RepoCallback<SubscriptionDto>() {
            @Override public void onSuccess(SubscriptionDto d) {
                cb.onSuccess(new SubscriptionItem(
                        d.id,
                        d.service_name != null ? d.service_name : "(이름 없음)",
                        d.logo_url, d.amount, d.start_date, d.repeat_cycle_days, d.auto_payment, d.is_shared
                ));
            }
            @Override public void onError(String m) { cb.onError(m); }
        });
    }
    public void deleteSubscription(long id, RepoCallback<Boolean> cb) {
        api.deleteSubscription(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override public void onResponse(Call<ApiResponse<Void>> c, Response<ApiResponse<Void>> r) {
                if (!r.isSuccessful()) { cb.onError("HTTP " + r.code()); return; }
                cb.onSuccess(true);
            }
            @Override public void onFailure(Call<ApiResponse<Void>> c, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    // ===== MyPage =====
    public void getMe(RepoCallback<UserProfile> cb){
        enqueue(api.getMe(), new RepoCallback<UserDto>() {
            @Override public void onSuccess(UserDto d) { cb.onSuccess(mapUser(d)); }
            @Override public void onError(String m) { cb.onError(m); }
        });
    }
    public void updateProfile(String name, @Nullable String email, RepoCallback<UserProfile> cb){
        enqueue(api.updateProfile(new UpdateProfileRequest(name, email)), new RepoCallback<UserDto>() {
            @Override public void onSuccess(UserDto d) { cb.onSuccess(mapUser(d)); }
            @Override public void onError(String m) { cb.onError(m); }
        });
    }
    public void changePassword(String currentPw, String newPw, RepoCallback<Boolean> cb){
        api.changePassword(new ChangePasswordRequest(currentPw, newPw))
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override public void onResponse(Call<ApiResponse<Void>> c, Response<ApiResponse<Void>> r) {
                        if (!r.isSuccessful()) { cb.onError("HTTP " + r.code()); return; }
                        ApiResponse<Void> body = r.body();
                        cb.onSuccess(body == null || body.success);
                    }
                    @Override public void onFailure(Call<ApiResponse<Void>> c, Throwable t) {
                        cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
                    }
                });
    }
    public void updateNotification(boolean enabled, RepoCallback<UserProfile> cb){
        enqueue(api.updateNotification(new NotificationSettingRequest(enabled)), new RepoCallback<UserDto>() {
            @Override public void onSuccess(UserDto d) { cb.onSuccess(mapUser(d)); }
            @Override public void onError(String m) { cb.onError(m); }
        });
    }
    public void deleteAccount(RepoCallback<Boolean> cb) {
        api.deleteMe().enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
            @Override public void onResponse(retrofit2.Call<ApiResponse<Void>> c,
                                             retrofit2.Response<ApiResponse<Void>> r) {
                if (!r.isSuccessful()) { cb.onError("HTTP " + r.code()); return; }
                // 보통 body 없거나 {success:true}
                cb.onSuccess(true);
            }
            @Override public void onFailure(retrofit2.Call<ApiResponse<Void>> c, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    // ===== 공통 =====
    private <T> void enqueue(Call<ApiResponse<T>> call, RepoCallback<T> cb){
        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override public void onResponse(Call<ApiResponse<T>> c, Response<ApiResponse<T>> r) {
                if (!r.isSuccessful()) { cb.onError("HTTP " + r.code()); return; }
                ApiResponse<T> body = r.body();
                if (body == null) { cb.onError("Empty response"); return; }
                if (body.success) cb.onSuccess(body.data);
                else cb.onError(body.message != null ? body.message : "Request failed");
            }
            @Override public void onFailure(Call<ApiResponse<T>> c, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    private UserProfile mapUser(UserDto d){
        if (d == null) return null;
        return new UserProfile(d.id, d.name, d.email, d.email_verified, d.status, d.notification_enabled);
    }

    private MultipartBody.Part toImagePart(Uri uri, String partName){
        try (InputStream is = app.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()){
            byte[] buf = new byte[8192]; int n;
            while((n=is.read(buf))!=-1) bos.write(buf,0,n);
            byte[] bytes = bos.toByteArray();
            String fileName = getDisplayName(uri);
            RequestBody body = RequestBody.create(MediaType.parse("image/*"), bytes);
            return MultipartBody.Part.createFormData(partName, fileName, body);
        } catch (Exception e){ throw new RuntimeException(e); }
    }
    private String getDisplayName(Uri uri){
        String fallback = "upload.jpg";
        Cursor c = app.getContentResolver().query(uri, null, null, null, null);
        if (c != null) {
            int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            String name = (idx >= 0 && c.moveToFirst()) ? c.getString(idx) : fallback;
            c.close();
            return name != null ? name : fallback;
        }
        return fallback;
    }
}