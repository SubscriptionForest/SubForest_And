package com.example.subforest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.subforest.network.ApiClient;
import com.example.subforest.network.ApiRepository;
import com.example.subforest.network.ApiService;
import com.example.subforest.model.LoginRequest;
import com.example.subforest.model.LoginResponse;
import com.example.subforest.network.TokenStore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginBtn, registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);

        // 로그인 버튼 클릭 → 백엔드 통신 후 홈 화면으로 이동
        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            LoginRequest loginRequest = new LoginRequest(email, password);

            ApiService apiService = ApiClient.get(this).create(ApiService.class);
            Call<LoginResponse> call = apiService.loginUser(loginRequest);

            call.enqueue(new Callback<LoginResponse>() {
                @Override public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse loginResponse = response.body();

                            // 토큰 저장
                            TokenStore ts = new TokenStore(getApplicationContext());
                            ts.saveAccessToken(loginResponse.getToken());

                            // FCM 토큰 등록 (실패해도 흐름 유지)
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(task -> {
                                        if (!task.isSuccessful()) {
                                            Log.w("FCM", "토큰 가져오기 실패", task.getException());
                                        } else {
                                            String fcm = task.getResult();
                                            Log.d("FCM", "토큰: " + fcm);
                                            ApiRepository.get(getApplicationContext())
                                                    .registerFcmToken(fcm, new ApiRepository.RepoCallback<Boolean>() {
                                                        @Override public void onSuccess(Boolean ok) { /* no-op */ }
                                                        @Override public void onError(String message) {
                                                            Log.w("FCM", "FCM 등록 실패: " + message);
                                                        }
                                                    });
                                        }
                                    });

                            // 내 정보 조회 (실패해도 홈으로 진입)
                            ApiRepository.get(getApplicationContext())
                                    .getMe(new ApiRepository.RepoCallback<ApiRepository.UserProfile>() {
                                        @Override public void onSuccess(ApiRepository.UserProfile me) {
                                            SharedPreferences sp = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
                                            sp.edit()
                                                    .putString("jwt_token", loginResponse.getToken())
                                                    .putString("user_email", me.email)
                                                    .putString("user_name", me.name)
                                                    .putLong("user_id", me.id)
                                                    .apply();

                                            Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                        }

                                        @Override public void onError(String msg) {
                                            Log.w("API", "내 정보 동기화 실패: " + msg);
                                            Toast.makeText(LoginActivity.this, "내 정보 동기화 실패", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                        }
                                    });

                        } else {
                            String err = null;
                            if (response.errorBody() != null) {
                                try { err = response.errorBody().string(); } catch (IOException ignore) {}
                            }
                            Toast.makeText(LoginActivity.this, "로그인 실패: 이메일/비밀번호 확인", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "일시적 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 새 계정 만들기 클릭 → 회원가입 화면으로 이동
        registerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}