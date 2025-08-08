package com.example.subforest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.example.subforest.R;

public class RegisterActivity extends AppCompatActivity {

    EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    Button signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameInput = findViewById(R.id.name_edittext);
        emailInput = findViewById(R.id.email_edittext);
        passwordInput = findViewById(R.id.password_edittext);
        confirmPasswordInput = findViewById(R.id.confirm_password_edittext);
        signupBtn = findViewById(R.id.signup_button);

        signupBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();

            if (!password.equals(confirmPassword)) {
                confirmPasswordInput.setError("비밀번호가 일치하지 않습니다.");
                return;
            }

            // 회원가입 처리 로직
            // 성공 시 로그인 화면으로 이동
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}