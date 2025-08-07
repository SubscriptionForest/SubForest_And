package com.example.subforest;

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
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MypageActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private static final String PREF_NAME = "UserSettings";
    private static final String PREF_NOTIFICATION = "notification_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        TextView tvNickname = findViewById(R.id.tvNickname);
        TextView tvEmail = findViewById(R.id.tvEmail);
        tvNickname.setText("홍길동");
        tvEmail.setText("hong@example.com");

        findViewById(R.id.btnChangePassword).setOnClickListener(v -> showPasswordDialog());
        findViewById(R.id.btnTerms).setOnClickListener(v -> showSimpleDialog());
        findViewById(R.id.btnDeactivate).setOnClickListener(v -> showConfirmDialog("계정을 비활성화 하시겠습니까?", true));
        findViewById(R.id.btnLogout).setOnClickListener(v -> showConfirmDialog("계정을 로그아웃 하시겠습니까?", false));

        Switch switchNotification = findViewById(R.id.switchNotification);
        TextView tvStatus = findViewById(R.id.tvNotificationStatus);
        boolean isEnabled = preferences.getBoolean(PREF_NOTIFICATION, true);
        switchNotification.setChecked(isEnabled);
        tvStatus.setText(isEnabled ? "on" : "off");
        tvStatus.setTextColor(Color.parseColor(isEnabled ? "#B8DDA1" : "#9E9E9E"));
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(PREF_NOTIFICATION, isChecked).apply();

            tvStatus.setText(isChecked ? "on" : "off");
            tvStatus.setTextColor(Color.parseColor(isChecked ? "#B8DDA1" : "#9E9E9E"));
        });

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

    private void showPasswordDialog() {
        Dialog dialog = createDialog();
        LinearLayout layout = createBaseLayout();

        TextView title = createTitle("비밀번호 변경");
        EditText etCurrent = createInput("현재 비밀번호");
        EditText etNew = createInput("새 비밀번호");
        EditText etConfirm = createInput("새 비밀번호 확인      ");

        Button btn = new Button(this);
        btn.setText("변경");
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B8DDA1")));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        p.setMargins(100, 70, 100, 0);
        btn.setLayoutParams(p);
        btn.setOnClickListener(v -> {
            String current = etCurrent.getText().toString().trim();
            String newPw = etNew.getText().toString().trim();
            String confirm = etConfirm.getText().toString().trim();

            if (current.isEmpty() || newPw.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            } else if (!newPw.equals(confirm)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
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
        String termsText = "1.  본 서비스는 이용자의 동의 하에 제공되며, 타인의 권리 침해, 불법적 이용, 시스템 방해 행위는 금지됩니다.\n"
                + "2.  본 서비스는 사전 공지 후 변경되거나 중단될 수 있으며, 개인정보는 관련 법령에 따라 안전하게 처리됩니다.\n\n"
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
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        btnParams.setMargins(15, 0, 15, 0); // 좌우 간격

        Button yes = new Button(this);
        yes.setText("예");
        yes.setLayoutParams(btnParams);
        yes.setOnClickListener(v -> {
            if (isDeactivate) {
                Toast.makeText(this, "계정이 비활성화 되었습니다", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
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
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setPadding(30, 25, 30, 25);
        input.setBackgroundResource(R.drawable.bg_edittext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 40, 0, 0);
        input.setLayoutParams(params);
        return input;
    }
}