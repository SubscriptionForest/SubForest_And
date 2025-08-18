package com.example.subforest.ui;

import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.subforest.R;
import com.example.subforest.network.ApiRepository;

import java.util.*;

public class AddActivity extends AppCompatActivity {

    private AutoCompleteTextView serviceNameAuto;
    private EditText amountEdit, dateEdit;
    private Spinner repeatSpinner;
    private Switch autoSwitch, shareSwitch;
    private Button saveButton;
    private ImageButton backButton;

    // 로컬 상태
    @Nullable private Long selectedServiceId = null;       // 공통 서비스 ID
    @Nullable private Long selectedCustomServiceId = null; // 사용자 서비스 ID
    @Nullable private Uri selectedImageUri = null;         // 커스텀 서비스 로고(옵션)

    // 자동완성 데이터
    private final List<ApiRepository.ServiceItem> serviceItems = new ArrayList<>();
    private final List<String> serviceNames = new ArrayList<>();
    private ServiceAdapter autoAdapter;

    // Dialog 내 프리뷰를 업데이트하기 위한 임시 참조
    @Nullable private ImageView dialogPreviewRef = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (dialogPreviewRef != null && selectedImageUri != null) {
                        dialogPreviewRef.setImageURI(selectedImageUri);
                    }
                }
            });

    private static final String[] SPINNER_LABELS = {"1개월", "3개월", "6개월", "1년"};
    private static final int[] SPINNER_DAYS = {30, 90, 180, 365};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        bindViews();
        initSpinner();
        initDatePicker();
        initSwitch(autoSwitch);
        initSwitch(shareSwitch);
        initAutoComplete();

        saveButton.setText("등록하기");
        saveButton.setOnClickListener(v -> save());
        backButton.setOnClickListener(v -> finish());

        // 서버에서 서비스 목록 로드
        loadServices();
    }

    private void bindViews() {
        serviceNameAuto = findViewById(R.id.serviceNameAuto);
        amountEdit      = findViewById(R.id.amountEdit);
        dateEdit        = findViewById(R.id.dateEdit);
        repeatSpinner   = findViewById(R.id.repeatSpinner);
        autoSwitch      = findViewById(R.id.autoSwitch);
        shareSwitch     = findViewById(R.id.shareSwitch);
        saveButton      = findViewById(R.id.saveButton);
        backButton      = findViewById(R.id.backButton);
    }

    private void initSpinner() {
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, SPINNER_LABELS);
        repeatSpinner.setAdapter(spAdapter);
        repeatSpinner.setSelection(0);
    }

    private void initDatePicker() {
        dateEdit.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) ->
                    dateEdit.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private void initSwitch(Switch sw) {
        updateSwitchColor(sw);
        sw.setOnCheckedChangeListener((b, c) -> updateSwitchColor(sw));
    }

    private void updateSwitchColor(Switch sw) {
        int color = sw.isChecked() ? Color.parseColor("#A5D6A7") : Color.LTGRAY;
        if (sw.getThumbDrawable() != null) sw.getThumbDrawable().setTint(color);
    }

    private void initAutoComplete() {
        autoAdapter = new ServiceAdapter(this, serviceNames);
        serviceNameAuto.setAdapter(autoAdapter);
        serviceNameAuto.setThreshold(0);
        serviceNameAuto.setOnTouchListener((v, e) -> {
            serviceNameAuto.showDropDown();
            return false;
        });
        serviceNameAuto.setOnItemClickListener((p, v, pos, id) -> {
            String item = (String) p.getItemAtPosition(pos);
            if ("추가하기".equals(item)) {
                serviceNameAuto.setText("");
                showAddDialog();
            } else {
                // 기존 서비스 선택됨 → serviceId 세팅, customServiceId/이미지 초기화
                selectedServiceId = findServiceIdByName(item);
                selectedCustomServiceId = null;
                selectedImageUri = null;
            }
        });
    }

    private void loadServices() {
        ApiRepository.get(this).getServices(null, new ApiRepository.RepoCallback<List<ApiRepository.ServiceItem>>() {
            @Override public void onSuccess(List<ApiRepository.ServiceItem> data) {
                serviceItems.clear();
                serviceItems.addAll(data);
                serviceNames.clear();
                for (ApiRepository.ServiceItem s : data) serviceNames.add(s.name);
                autoAdapter.refresh(); // 드롭다운 업데이트
            }
            @Override public void onError(String message) {
                Toast.makeText(AddActivity.this, "서비스 목록 실패: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
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
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(240, 110);
        pickBtn.setLayoutParams(btnParams);
        pickBtn.setOnClickListener(v -> {
            dialogPreviewRef = preview;
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
                params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
                window.setAttributes(params);
            }

            Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setOnClickListener(v -> {
                String name = nameInput.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(this, "서비스명을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 서버에 커스텀 서비스 생성
                ApiRepository.get(this).createCustomService(name, selectedImageUri,
                        new ApiRepository.RepoCallback<ApiRepository.CustomServiceItem>() {
                            @Override public void onSuccess(ApiRepository.CustomServiceItem cs) {
                                // 자동완성 목록에 추가
                                if (!serviceNames.contains(cs.name)) {
                                    serviceNames.add(cs.name);
                                    autoAdapter.refresh();
                                }
                                serviceNameAuto.setText(cs.name);
                                serviceNameAuto.dismissDropDown();

                                selectedServiceId = null;
                                selectedCustomServiceId = cs.id; // 이후 저장 시 사용
                                Toast.makeText(AddActivity.this, "등록되었습니다.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                            @Override public void onError(String msg) {
                                Toast.makeText(AddActivity.this, "등록 실패: " + msg, Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        });

        dialog.show();
    }

    private void save() {
        String name  = serviceNameAuto.getText().toString().trim();
        String sAmt  = amountEdit.getText().toString().trim();
        String date  = dateEdit.getText().toString().trim();
        boolean auto = autoSwitch.isChecked();
        boolean shared = shareSwitch.isChecked();

        if (name.isEmpty() || sAmt.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int amount;
        try { amount = Integer.parseInt(sAmt.replaceAll("[^0-9]", "")); }
        catch (NumberFormatException e) { Toast.makeText(this, "금액을 확인하세요.", Toast.LENGTH_SHORT).show(); return; }

        int pos = Math.max(0, repeatSpinner.getSelectedItemPosition());
        int repeatDays = SPINNER_DAYS[pos];

        // 서비스 선택 상태 정리
        Long serviceId = selectedServiceId;
        Long customServiceId = selectedCustomServiceId;

        // 자동완성에서 기존 서비스명을 직접 타이핑한 경우(선택 이벤트 없이) 보정
        if (serviceId == null && customServiceId == null) {
            Long maybe = findServiceIdByName(name);
            if (maybe != null) serviceId = maybe;
        }

        // 커스텀 서비스가 필요한데 아직 생성 안 했으면 자동 생성 후 구독 생성
        if (serviceId == null && customServiceId == null) {
            ApiRepository.get(this).createCustomService(name, selectedImageUri,
                    new ApiRepository.RepoCallback<ApiRepository.CustomServiceItem>() {
                        @Override public void onSuccess(ApiRepository.CustomServiceItem cs) {
                            createSub(null, cs.id, amount, date, repeatDays, auto, shared);
                        }
                        @Override public void onError(String msg) {
                            Toast.makeText(AddActivity.this, "서비스 생성 실패: " + msg, Toast.LENGTH_SHORT).show();
                        }
                    });
            return;
        }

        // 바로 구독 생성
        createSub(serviceId, customServiceId, amount, date, repeatDays, auto, shared);
    }

    private void createSub(@Nullable Long serviceId, @Nullable Long customServiceId,
                           int amount, String startYmd, int repeatDays,
                           boolean auto, boolean shared) {
        ApiRepository.get(this).createSubscription(serviceId, customServiceId, amount, startYmd, repeatDays, auto, shared,
                new ApiRepository.RepoCallback<ApiRepository.SubscriptionItem>() {
                    @Override public void onSuccess(ApiRepository.SubscriptionItem s) {
                        Toast.makeText(AddActivity.this, "등록되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(AddActivity.this, "등록 실패: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Nullable
    private Long findServiceIdByName(String name) {
        for (ApiRepository.ServiceItem s : serviceItems) {
            if (s.name.equalsIgnoreCase(name)) return s.id;
        }
        return null;
    }

    // ====== 자동완성 어댑터 ======
    static class ServiceAdapter extends ArrayAdapter<String> {
        private final List<String> source;
        private final List<String> filtered = new ArrayList<>();

        ServiceAdapter(AppCompatActivity context, List<String> list) {
            super(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
            this.source = list;
        }

        void refresh() { getFilter().filter(""); }

        @Override public int getCount() { return filtered.size(); }
        @Override public String getItem(int pos) { return filtered.get(pos); }

        @Override public Filter getFilter() {
            return new Filter() {
                @Override protected FilterResults performFiltering(CharSequence c) {
                    filtered.clear();
                    String q = c == null ? "" : c.toString().toLowerCase(Locale.ROOT);
                    for (String s : source) if (s.toLowerCase(Locale.ROOT).contains(q)) filtered.add(s);
                    if (!filtered.contains("추가하기")) filtered.add("추가하기");
                    FilterResults r = new FilterResults();
                    r.values = filtered; r.count = filtered.size();
                    return r;
                }
                @Override protected void publishResults(CharSequence c, FilterResults r) {
                    clear(); addAll(filtered); notifyDataSetChanged();
                }
            };
        }
    }
}