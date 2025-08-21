package com.example.subforest.network;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.subforest.App;
import com.example.subforest.HomeActivity;
import com.example.subforest.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class SubForestFirebaseService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        // 로그인 상태면 서버에 토큰 재등록
        Context app = getApplicationContext();
        String jwt = new TokenStore(app).getAccessToken();
        if (jwt != null && !jwt.isEmpty()) {
            ApiRepository.get(app).registerFcmToken(token, new ApiRepository.RepoCallback<Boolean>() {
                @Override public void onSuccess(Boolean ok) {}
                @Override public void onError(String message) {}
            });
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage msg) {
        // notification 페이로드 or data 페이로드에서 제목/본문 추출
        String title = msg.getNotification() != null ? msg.getNotification().getTitle() : null;
        String body  = msg.getNotification() != null ? msg.getNotification().getBody()  : null;
        if (title == null) title = msg.getData().get("title");
        if (body  == null) body  = msg.getData().get("body");

        // 탭하면 열 화면
        Intent i = new Intent(this, HomeActivity.class);
        int flags = android.os.Build.VERSION.SDK_INT >= 23
                ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, flags);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title != null ? title : "구독 알림")
                .setContentText(body != null ? body : "새 알림이 도착했습니다")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(this).notify((int)System.currentTimeMillis(), nb.build());
    }
}