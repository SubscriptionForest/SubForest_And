package com.example.subforest;

import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddActivity extends AppCompatActivity {

    AutoCompleteTextView serviceNameAuto;
    EditText amountEdit, dateEdit;
    Spinner repeatSpinner;
    Switch autoSwitch, shareSwitch;
    Button saveButton;
    ImageView backButton;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        serviceNameAuto = findViewById(R.id.serviceNameAuto);
        amountEdit = findViewById(R.id.amountEdit);
        dateEdit = findViewById(R.id.dateEdit);
        repeatSpinner = findViewById(R.id.repeatSpinner);
        autoSwitch = findViewById(R.id.autoSwitch);
        shareSwitch = findViewById(R.id.shareSwitch);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);

        // 서비스명 자동완성
        String[] services = {"YouTube", "Netflix", "Spotify", "Disney+", "Watcha"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, services);
        serviceNameAuto.setAdapter(adapter);

        // 달력 선택
        calendar = Calendar.getInstance();
        dateEdit.setInputType(InputType.TYPE_NULL);
        dateEdit.setOnClickListener(v -> {
            int y = calendar.get(Calendar.YEAR);
            int m = calendar.get(Calendar.MONTH);
            int d = calendar.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String selectedDate = String.format("%04d/%02d/%02d", year, month + 1, dayOfMonth);
                dateEdit.setText(selectedDate);
            }, y, m, d).show();
        });

        // 반복주기 설정
        String[] periods = {"1개월", "3개월", "6개월", "1년"};
        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, periods);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatSpinner.setAdapter(repeatAdapter);

        // 뒤로가기 버튼
        backButton.setOnClickListener(v -> finish());

        // 저장 버튼
        saveButton.setOnClickListener(v -> {
            String serviceName = serviceNameAuto.getText().toString();
            String amount = amountEdit.getText().toString();
            String date = dateEdit.getText().toString();
            String repeat = repeatSpinner.getSelectedItem().toString();
            boolean isAuto = autoSwitch.isChecked();
            boolean isShared = shareSwitch.isChecked();

            // 저장 로직

            Toast.makeText(this, "구독이 등록되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}