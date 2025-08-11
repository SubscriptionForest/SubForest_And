package com.example.subforest.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.subforest.HomeActivity;
import com.example.subforest.LoginActivity;
import com.example.subforest.R;
import com.example.subforest.network.ApiRepository;
import com.example.subforest.network.TokenStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MypageActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private static final String PREF_NAME = "UserSettings";
    private static final String PREF_NOTIFICATION = "notification_enabled";

    private TextView tvNickname;
    private TextView tvEmail;
    private Switch switchNotification;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        tvNickname = findViewById(R.id.tvNickname);
        tvEmail = findViewById(R.id.tvEmail);
        switchNotification = findViewById(R.id.switchNotification);
        tvStatus = findViewById(R.id.tvNotificationStatus);

        // 서버에서 내 정보 로드
        loadMe();

        findViewById(R.id.btnChangePassword).setOnClickListener(v -> showPasswordDialog());
        findViewById(R.id.btnTerms).setOnClickListener(v -> showSimpleDialog());
        findViewById(R.id.btnDeactivate).setOnClickListener(v -> showConfirmDialog("계정을 비활성화 하시겠습니까?", true));
        findViewById(R.id.btnLogout).setOnClickListener(v -> showConfirmDialog("로그아웃 하시겠습니까?", false));

        // 알림 스위치 초기화 (로컬 기본값)
        boolean isEnabledLocal = preferences.getBoolean(PREF_NOTIFICATION, true);
        switchNotification.setChecked(isEnabledLocal);
        renderNotifyStatus(isEnabledLocal);

        // 알림 스위치 변경 -> 서버 반영
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 로컬 즉시 반영
            preferences.edit().putBoolean(PREF_NOTIFICATION, isChecked).apply();
            renderNotifyStatus(isChecked);
            // 서버 반영
            ApiRepository.get(this).updateNotification(isChecked, new ApiRepository.RepoCallback<ApiRepository.UserProfile>() {
                @Override public void onSuccess(ApiRepository.UserProfile up) {
                    // 성공
                }
                @Override public void onError(String msg) {
                    Toast.makeText(MypageActivity.this, "알림 설정 실패: " + msg, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 하단 네비게이션
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setSelectedItemId(R.id.nav_profile);
        nav.setOnItemSelectedListener(item -> {
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

    private void loadMe() {
        ApiRepository.get(this).getMe(new ApiRepository.RepoCallback<ApiRepository.UserProfile>() {
            @Override public void onSuccess(ApiRepository.UserProfile me) {
                if (me != null) {
                    tvNickname.setText(me.name != null ? me.name : "");
                    tvEmail.setText(me.email != null ? me.email : "");
                    // 서버 상태가 로컬과 다르면 맞춰줌
                    if (me.notificationEnabled != switchNotification.isChecked()) {
                        switchNotification.setChecked(me.notificationEnabled);
                        preferences.edit().putBoolean(PREF_NOTIFICATION, me.notificationEnabled).apply();
                        renderNotifyStatus(me.notificationEnabled);
                    }
                }
            }
            @Override public void onError(String msg) {
                Toast.makeText(MypageActivity.this, "내 정보 조회 실패: " + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderNotifyStatus(boolean enabled) {
        tvStatus.setText(enabled ? "on" : "off");
        tvStatus.setTextColor(Color.parseColor(enabled ? "#B8DDA1" : "#9E9E9E"));
    }

    private void showPasswordDialog() {
        Dialog dialog = createDialog();
        LinearLayout layout = createBaseLayout();

        TextView title = createTitle("비밀번호 변경");
        EditText etCurrent = createInput("현재 비밀번호");
        EditText etNew = createInput("새 비밀번호");
        EditText etConfirm = createInput("새 비밀번호 확인");

        etCurrent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etNew.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        Button btn = new Button(this);
        btn.setText("변경");
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B8DDA1")));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p.setMargins(100, 70, 100, 0);
        btn.setLayoutParams(p);
        btn.setOnClickListener(v -> {
            String current = text(etCurrent);
            String newPw = text(etNew);
            String confirm = text(etConfirm);

            if (current.isEmpty() || newPw.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            } else if (!newPw.equals(confirm)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            } else {
                ApiRepository.get(this).changePassword(current, newPw, new ApiRepository.RepoCallback<Boolean>() {
                    @Override public void onSuccess(Boolean ok) {
                        Toast.makeText(MypageActivity.this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(MypageActivity.this, "변경 실패: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        layout.addView(title);
        layout.addView(etCurrent);
        layout.addView(etNew);
        layout.addView(etConfirm);
        layout.addView(btn);

        dialog.setContentView(layout);
        dialog.show();
    }

    private void showSimpleDialog() {
        Dialog dialog = createDialog();
        LinearLayout layout = createBaseLayout();

        TextView title = createTitle("서비스 이용 약관");
        TextView message = new TextView(this);
        String termsText = "1. 본 서비스는 이용자의 동의 하에 제공되며, 타인의 권리 침해, 불법적 이용, 시스템 방해 행위는 금지됩니다.\n"
                + "2. 본 서비스는 사전 공지 후 변경되거나 중단될 수 있으며, 개인정보는 관련 법령에 따라 안전하게 처리됩니다.\n\n"
                + "서비스 이용 시 본 약관에 동의한 것으로 간주됩니다.";
        message.setText(termsText);
        message.setTextSize(16);
        message.setTextColor(Color.DKGRAY);
        message.setPadding(0, 40, 0, 70);

        Button btn = new Button(this);
        btn.setText("확인");
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B8DDA1")));
        btn.setOnClickListener(v -> dialog.dismiss());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p.setMargins(90, 40, 90, 10);
        btn.setLayoutParams(p);

        layout.addView(title);
        layout.addView(message);
        layout.addView(btn);

        dialog.setContentView(layout);
        dialog.show();
    }

    private void showConfirmDialog(String message, boolean isDeactivate) {
        Dialog dialog = createDialog();
        LinearLayout layout = createBaseLayout();

        TextView title = createTitle(message);
        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.setGravity(Gravity.CENTER);
        btnLayout.setPadding(0, 40, 0, 0);
        LinearLayout.LayoutParams btnParams =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        btnParams.setMargins(15, 0, 15, 0);

        Button yes = new Button(this);
        yes.setText("예");
        yes.setLayoutParams(btnParams);
        yes.setOnClickListener(v -> {
            if (isDeactivate) {
                // 계정 탈퇴
                ApiRepository.get(this).deleteAccount(new ApiRepository.RepoCallback<Boolean>() {
                    @Override public void onSuccess(Boolean ok) {
                        new TokenStore(getApplicationContext()).clear();
                        Toast.makeText(MypageActivity.this, "계정이 탈퇴되었습니다.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        goLoginClearTask();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(MypageActivity.this, "탈퇴 실패: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // 로그아웃
                new TokenStore(getApplicationContext()).clear();
                Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                goLoginClearTask();
            }
        });

        Button no = new Button(this);
        no.setText("아니요");
        no.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B8DDA1")));
        no.setLayoutParams(btnParams);
        no.setOnClickListener(v -> dialog.dismiss());

        btnLayout.addView(yes);
        btnLayout.addView(no);

        layout.addView(title);
        layout.addView(btnLayout);

        dialog.setContentView(layout);
        dialog.show();
    }

    private void goLoginClearTask() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private Dialog createDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.y = 150;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
        }
        return dialog;
    }

    private LinearLayout createBaseLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(110, 80, 110, 80);
        layout.setBackgroundResource(R.drawable.bg_dialog);
        return layout;
    }

    private TextView createTitle(String txt) {
        TextView title = new TextView(this);
        title.setText(txt);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.BLACK);
        title.setPadding(0, 0, 0, 35);
        return title;
    }

    private EditText createInput(String hint) {
        EditText input = new EditText(this);
        SpannableString hintStr = new SpannableString(hint);
        hintStr.setSpan(new AbsoluteSizeSpan(15, true), 0, hint.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        input.setHint(hintStr);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setPadding(30, 25, 30, 25);
        input.setBackgroundResource(R.drawable.bg_edittext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 40, 0, 0);
        input.setLayoutParams(params);
        return input;
    }

    private static String text(@Nullable EditText et) {
        return et == null || et.getText() == null ? "" : et.getText().toString().trim();
    }
}