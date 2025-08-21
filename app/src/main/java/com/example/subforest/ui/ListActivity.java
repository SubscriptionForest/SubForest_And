package com.example.subforest.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.subforest.HomeActivity;
import com.example.subforest.R;
import com.example.subforest.network.ApiRepository;
import com.example.subforest.network.TokenStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SubscriptionAdapter adapter;
    private Spinner sortSpinner;

    private final List<ApiRepository.SubscriptionItem> current = new ArrayList<>();
    private static final String[] SORTS = {"날짜 순", "이름 순", "금액 순"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        sortSpinner = findViewById(R.id.sortSpinner);
        recyclerView = findViewById(R.id.subscriptionRecyclerView);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, SORTS
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);
        sortSpinner.setSelection(0);
        sortSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                applySort(position);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubscriptionAdapter(new SubscriptionAdapter.Listener() {
            @Override public void onEdit(ApiRepository.SubscriptionItem item) {
                Intent i = new Intent(ListActivity.this, EditSubActivity.class);
                i.putExtra("id", item.id);
                i.putExtra("name", item.name);
                i.putExtra("amount", item.amount);
                i.putExtra("startDate", item.startDate);
                i.putExtra("repeatDays", item.repeatDays);
                i.putExtra("autoPayment", item.autoPayment);
                i.putExtra("shared", item.shared);
                startActivity(i);
            }
            @Override public void onDelete(ApiRepository.SubscriptionItem item) {
                showDeleteConfirmDialog(item);
            }
            @Override public void onClick(ApiRepository.SubscriptionItem item) {}
        });
        recyclerView.setAdapter(adapter);

        load();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_list);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, MypageActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_list) {
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();   // 돌아올 때마다 새로고침
    }

    private void load() {
        ApiRepository.get(this).getSubscriptions(
                new ApiRepository.RepoCallback<List<ApiRepository.SubscriptionItem>>() {
                    @Override public void onSuccess(List<ApiRepository.SubscriptionItem> data) {
                        current.clear();
                        current.addAll(data);
                        applySort(sortSpinner.getSelectedItemPosition());
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(ListActivity.this, "불러오기 실패: " + msg, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void applySort(int position) {
        List<ApiRepository.SubscriptionItem> copy = new ArrayList<>(current);
        switch (position) {
            case 0: // 날짜 순
                copy.sort(Comparator.comparingLong(ListActivity::nextPaymentEpoch));
                break;
            case 1: // 이름 순
                copy.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
                break;
            case 2: // 금액 순
                copy.sort((a, b) -> Integer.compare(b.amount, a.amount));
                break;
        }
        adapter.submitList(copy);
    }

    // 다음 결제일 = startDate + repeatDays
    private static long nextPaymentEpoch(@NonNull ApiRepository.SubscriptionItem it) {
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date start = iso.parse(it.startDate);
            long base = start != null ? start.getTime() : 0L;
            long millis = (long) it.repeatDays * 24L * 60L * 60L * 1000L;
            return base + millis;
        } catch (ParseException e) {
            return Long.MAX_VALUE;
        }
    }

    private void showDeleteConfirmDialog(ApiRepository.SubscriptionItem item) {
        Dialog dialog = createDialog();
        LinearLayout layout = createBaseLayout();
        TextView title = createTitle(item.name + " 구독을 삭제하시겠습니까?");
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
            performDelete(item);
            dialog.dismiss();
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

    private void performDelete(ApiRepository.SubscriptionItem item) {
        if (item == null || item.id <= 0) {
            Toast.makeText(this, "삭제할 항목을 다시 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiRepository.get(this).deleteSubscription(item.id, new ApiRepository.RepoCallback<Boolean>() {
            @Override public void onSuccess(Boolean ok) {
                for (Iterator<ApiRepository.SubscriptionItem> it = current.iterator(); it.hasNext();) {
                    if (it.next().id == item.id) {
                        it.remove();
                        break;
                    }
                }
                applySort(sortSpinner.getSelectedItemPosition());
                Toast.makeText(ListActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(String msg) {
                Toast.makeText(ListActivity.this, "삭제 실패: " + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Dialog createDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            //params.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
        }
        return dialog;
    }

    private LinearLayout createBaseLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(100, 80, 100, 80);
        layout.setBackgroundResource(R.drawable.bg_dialog);
        return layout;
    }

    private TextView createTitle(String txt) {
        TextView title = new TextView(this);
        title.setText(txt);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.BLACK);
        title.setPadding(0, 0, 0, 20);
        title.setGravity(Gravity.CENTER);
        return title;
    }
}