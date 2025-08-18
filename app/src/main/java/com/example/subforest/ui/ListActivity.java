package com.example.subforest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.subforest.HomeActivity;
import com.example.subforest.R;
import com.example.subforest.network.ApiRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SubscriptionAdapter adapter;
    private Spinner sortSpinner;

    private final List<ApiRepository.SubscriptionItem> current = new ArrayList<>();
    private static final String[] SORTS = {"결제일 순", "이름 순", "금액 순"};

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
                ApiRepository.get(ListActivity.this).deleteSubscription(item.id, new ApiRepository.RepoCallback<Boolean>() {
                    @Override public void onSuccess(Boolean ok) {
                        Toast.makeText(ListActivity.this, "삭제됨", Toast.LENGTH_SHORT).show();
                        load();
                    }
                    @Override public void onError(String msg) {
                        Toast.makeText(ListActivity.this, "삭제 실패: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
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
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, MypageActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_list) {
                return true;
            }
            return false;
        });
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
            case 0: // 결제일 순
                copy.sort(Comparator.comparingLong(ListActivity::nextPaymentEpoch));
                break;
            case 1: // 이름 순
                copy.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
                break;
            case 2: // 금액 순
                copy.sort(Comparator.comparingInt((ApiRepository.SubscriptionItem a) -> a.amount).reversed());
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
}