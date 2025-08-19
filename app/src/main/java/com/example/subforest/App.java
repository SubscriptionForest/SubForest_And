package com.example.subforest;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.example.subforest.network.ApiRepository;
import com.example.subforest.network.TokenStore;

public class App extends Application {
    public static final String CHANNEL_ID = "subforest_default";

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "구독숲 알림", NotificationManager.IMPORTANCE_HIGH);
            ch.enableVibration(true);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }

        // 토큰만 있고 userId 없는 상태면 자동 동기화
        TokenStore ts = new TokenStore(getApplicationContext());
        String token = ts.getAccessToken();
        long uid = ts.getUserIdOr(-1L);
        if (token != null && !token.isEmpty() && uid <= 0) {
            ApiRepository.get(getApplicationContext()).getMe(new ApiRepository.RepoCallback<ApiRepository.UserProfile>() {
                @Override public void onSuccess(ApiRepository.UserProfile me) { }
                @Override public void onError(String message) { }
            });
        }
    }
}