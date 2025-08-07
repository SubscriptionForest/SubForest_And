package com.example.subforest;

import android.app.*;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.*;

public class AddActivity extends AppCompatActivity {

    AutoCompleteTextView serviceNameAuto;
    EditText amountEdit, dateEdit;
    Spinner repeatSpinner;
    Switch autoSwitch, shareSwitch;
    Button saveButton;
    Uri selectedImageUri;

    List<String> serviceList = new ArrayList<>(Arrays.asList("Netflix", "YouTube", "Disney+"));

    ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null)
                    selectedImageUri = result.getData().getData();
            });

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

        initAutoComplete();
        initDatePicker();
        initSpinner();
        initSwitch(autoSwitch);
        initSwitch(shareSwitch);

        saveButton.setOnClickListener(v -> save());
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    void initAutoComplete() {
        ServiceAdapter adapter = new ServiceAdapter(this, serviceList);
        serviceNameAuto.setAdapter(adapter);
        serviceNameAuto.setThreshold(0);
        serviceNameAuto.setOnTouchListener((v, e) -> {
            serviceNameAuto.showDropDown();
            return false;
        });
        serviceNameAuto.setOnItemClickListener((p, v, pos, id) -> {
            String item = (String) p.getItemAtPosition(pos);
            if (item.equals("추가하기")) {
                serviceNameAuto.setText("");
                showAddDialog();
            }
        });
    }

    void showAddDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(80, 60, 200, 0);

        EditText nameInput = new EditText(this);
        nameInput.setHint("서비스명");
        nameInput.setTextSize(16);
        nameInput.setPadding(30, 25, 30, 25);
        nameInput.setBackgroundResource(R.drawable.bg_edittext);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nameParams.setMargins(0, 0, 0, 30);
        nameInput.setLayoutParams(nameParams);
        layout.addView(nameInput);

        LinearLayout imageRow = new LinearLayout(this);
        imageRow.setOrientation(LinearLayout.HORIZONTAL);
        imageRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView preview = new ImageView(this);
        preview.setImageResource(R.drawable.ic_subforest);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(200, 200);
        imgParams.setMargins(0, 0, 25, 0);
        preview.setLayoutParams(imgParams);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Button pickBtn = new Button(this);
        pickBtn.setText("사진 추가");
        pickBtn.setTextColor(ContextCompat.getColor(this, R.color.white));
        pickBtn.setBackgroundResource(R.drawable.bg_green);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(200, 100);
        pickBtn.setLayoutParams(btnParams);
        pickBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        imageRow.addView(preview);
        imageRow.addView(pickBtn);
        layout.addView(imageRow);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setTitle("구독 서비스 추가")
                .setPositiveButton("등록", null)
                .setNegativeButton("취소", null)
                .create();

        dialog.setOnShowListener(d -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.drawable.bg_dialog);
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = (int)(getResources().getDisplayMetrics().widthPixels * 0.8);
                window.setAttributes(params);
            }

            Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setOnClickListener(v -> {
                String name = nameInput.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(this, "서비스명을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!serviceList.contains(name)) {
                    serviceList.add(name);
                }
                serviceNameAuto.setText(name);
                serviceNameAuto.dismissDropDown();
                Toast.makeText(this, "등록되었습니다.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    void initDatePicker() {
        dateEdit.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) ->
                    dateEdit.setText(String.format("%04d-%02d-%02d", y, m + 1, d)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    void initSpinner() {
        String[] items = {"1개월", "3개월", "6개월", "1년"};
        repeatSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, items));
    }

    void initSwitch(Switch sw) {
        updateSwitchColor(sw);
        sw.setOnCheckedChangeListener((b, c) -> updateSwitchColor(sw));
    }

    void updateSwitchColor(Switch sw) {
        int color = sw.isChecked() ? Color.parseColor("#A5D6A7") : Color.LTGRAY;
        sw.getThumbDrawable().setTint(color);
    }

    void save() {
        String name = serviceNameAuto.getText().toString().trim();
        String amount = amountEdit.getText().toString().trim();
        String date = dateEdit.getText().toString().trim();

        if (name.isEmpty() || amount.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "등록되었습니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    static class ServiceAdapter extends ArrayAdapter<String> {
        List<String> allItems, filtered = new ArrayList<>();
        public ServiceAdapter(Activity context, List<String> list) {
            super(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
            this.allItems = new ArrayList<>(list);
        }
        @Override public int getCount() { return filtered.size(); }
        @Override public String getItem(int pos) { return filtered.get(pos); }
        @Override public Filter getFilter() {
            return new Filter() {
                @Override protected FilterResults performFiltering(CharSequence c) {
                    filtered.clear();
                    String q = c == null ? "" : c.toString().toLowerCase();
                    for (String s : allItems)
                        if (s.toLowerCase().contains(q)) filtered.add(s);
                    if (!filtered.contains("추가하기")) filtered.add("추가하기");
                    FilterResults r = new FilterResults();
                    r.values = filtered; r.count = filtered.size();
                    return r;
                }
                @Override protected void publishResults(CharSequence c, FilterResults r) {
                    clear();
                    addAll(filtered);
                    notifyDataSetChanged();
                }
            };
        }
    }
}