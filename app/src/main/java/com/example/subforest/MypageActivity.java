package com.example.subforest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MypageActivity extends AppCompatActivity {

    TextView username, email, passwordChange;
    Switch notificationSwitch;
    LinearLayout termsLayout, deactivateLayout, logoutLayout;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        // 뷰 연결
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        passwordChange = findViewById(R.id.passwordChange);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        termsLayout = findViewById(R.id.termsLayout);
        deactivateLayout = findViewById(R.id.deactivateLayout);
        logoutLayout = findViewById(R.id.logoutLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // 기본 데이터 설정
        username.setText("닉네임 님");
        email.setText("email@sample.com");

        // 알림 설정 스위치 리스너
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, isChecked ? "알림 ON" : "알림 OFF", Toast.LENGTH_SHORT).show();
        });

        // 비밀번호 변경 클릭
        passwordChange.setOnClickListener(v -> {
            Toast.makeText(this, "비밀번호 변경 화면으로 이동", Toast.LENGTH_SHORT).show();
        });

        // 약관 클릭
        termsLayout.setOnClickListener(v -> {
            Toast.makeText(this, "서비스 이용 약관 보기", Toast.LENGTH_SHORT).show();
        });

        // 계정 비활성화 클릭
        deactivateLayout.setOnClickListener(v -> {
            Toast.makeText(this, "계정 비활성화 처리", Toast.LENGTH_SHORT).show();
        });

        // 로그아웃
        logoutLayout.setOnClickListener(v -> {
            startActivity(new Intent(MypageActivity.this, LoginActivity.class));
            finish();
        });

        // 하단 네비게이션 작동
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_list) {
                startActivity(new Intent(this, ListActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }
}