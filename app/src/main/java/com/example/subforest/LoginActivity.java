// LoginActivity.java
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

        // 로그인 버튼 클릭 → 백엔드 통신 후 홈 화면으로 이동한다.
        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            // LoginRequest 객체 생성 (API 명세에 맞춤)
            LoginRequest loginRequest = new LoginRequest(email, password);

            ApiService apiService = ApiClient.get(this).create(ApiService.class);
            Call<LoginResponse> call = apiService.loginUser(loginRequest);

            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse loginResponse = response.body();
                        TokenStore ts = new TokenStore(getApplicationContext());
                        ts.saveAccessToken(loginResponse.getToken());
                        // FCM 토큰 등록
                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(task -> {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "FCM 토큰 가져오기 실패", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    String token = task.getResult();
                                    Log.d("FCM", "토큰: " + token);
                                    ApiRepository.get(getApplicationContext())
                                            .registerFcmToken(token, new ApiRepository.RepoCallback<Boolean>() {
                                                @Override public void onSuccess(Boolean ok) {}
                                                @Override public void onError(String message) { Toast.makeText(LoginActivity.this, "FCM 등록 실패", Toast.LENGTH_SHORT).show(); }
                                            });
                                });

                        ApiRepository.get(getApplicationContext())
                                .getMe(new ApiRepository.RepoCallback<ApiRepository.UserProfile>() {
                                    @Override public void onSuccess(ApiRepository.UserProfile me) {
                                        // 토큰 및 사용자 정보 SharedPreferences에 저장
                                        SharedPreferences sp = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sp.edit();
                                        editor.putString("jwt_token", loginResponse.getToken());
                                        editor.putString("user_email", loginResponse.getEmail());
                                        editor.putString("user_name", loginResponse.getName());
                                        editor.apply();

                                        Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    }
                                    @Override public void onError(String msg) {
                                        // 유저 동기화 실패해도 일단 진입
                                        Toast.makeText(LoginActivity.this, "내 정보 동기화 실패: " + msg, Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    }
                                });
                    } else {
                        Toast.makeText(LoginActivity.this, "로그인 실패: 이메일 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
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