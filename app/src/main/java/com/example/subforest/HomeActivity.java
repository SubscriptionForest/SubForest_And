package com.example.subforest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.subforest.adapter.PaymentServiceAdapter;
import com.example.subforest.adapter.SubscribedServiceAdapter;
import com.example.subforest.model.DashboardSummaryResponse;
import com.example.subforest.model.SubscribedService;
import com.example.subforest.model.UpcomingSubscriptionResponse;
import com.example.subforest.network.ApiClient;
import com.example.subforest.network.ApiDtos.PagedList;
import com.example.subforest.network.ApiDtos.SubscriptionListItemDto;
import com.example.subforest.network.ApiService;
import com.example.subforest.ui.AddActivity;
import com.example.subforest.ui.ListActivity;
import com.example.subforest.ui.MypageActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements OnChartValueSelectedListener { // 리스너 인터페이스 구현

    private TextView greetingText, totalCostText;
    private PieChart pieChart;
    private RecyclerView subscribedServicesRecyclerView, paymentRecyclerView;
    private Button addSubscriptionBtn;
    private BottomNavigationView bottomNavigationView;
    private String userName; // 사용자 이름

    @Nullable private View emptyChartView; // 필요 시 사용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // UI 요소 초기화
        greetingText = findViewById(R.id.greetingText);
        totalCostText = findViewById(R.id.totalCostText);
        pieChart = findViewById(R.id.pieChart);
        addSubscriptionBtn = findViewById(R.id.addSubscriptionBtn);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        subscribedServicesRecyclerView = findViewById(R.id.subscribedServicesRecyclerView);
        paymentRecyclerView = findViewById(R.id.paymentRecyclerView);
        // emptyChartView = findViewById(R.id.emptyChartView);

        // RecyclerView 설정
        setupRecyclerViews();

        // 사용자 이름 가져오기
        SharedPreferences sp = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        userName = sp.getString("user_name", "00");
        greetingText.setText(String.format("안녕하세요. %s님!", userName));

        // 백엔드 데이터 로드
        loadDashboardSummary();
        loadUpcomingPayments();
        loadSubscribedServices();

        // 버튼 클릭 리스너
        addSubscriptionBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddActivity.class);
            startActivity(intent);
        });

        // 하단 네비게이션
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_list) {
                // ListActivity로 이동
                startActivity(new Intent(this, ListActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                // MypageActivity로 이동
                startActivity(new Intent(this, MypageActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_home) {
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardSummary();
        loadUpcomingPayments();
        loadSubscribedServices();
    }

    private void setupRecyclerViews() {
        // 구독 중인 서비스 RecyclerView (2x2 그리드)
        subscribedServicesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        subscribedServicesRecyclerView.setAdapter(new SubscribedServiceAdapter(new ArrayList<>()));

        // 결제 예정 서비스 RecyclerView (가로 스크롤)
        paymentRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        paymentRecyclerView.setAdapter(new PaymentServiceAdapter(new ArrayList<>()));
    }

    private void loadDashboardSummary() {
        // userId 가져오기
        long userId = getUserId();
        if (userId == -1) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            setupPieChart(Collections.emptyMap());
            updateMonthlyCost(0);
            return;
        }

        ApiService api = ApiClient.get(this).create(ApiService.class);
        api.getDashboardSummary(userId).enqueue(new Callback<DashboardSummaryResponse>() {
            @Override public void onResponse(Call<DashboardSummaryResponse> call, Response<DashboardSummaryResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    DashboardSummaryResponse s = resp.body();
                    // 총액
                    int amount = s.getTotalMonthlySpend();
                    updateMonthlyCost(amount);
                    // 파이 차트 데이터
                    setupPieChart(s.toChartMap());
                } else {
                    Toast.makeText(HomeActivity.this, "대시보드 데이터 로드 실패(" + resp.code() + ")", Toast.LENGTH_SHORT).show();
                    updateMonthlyCost(0);
                    setupPieChart(Collections.emptyMap());
                }
            }

            @Override
            public void onFailure(Call<DashboardSummaryResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateMonthlyCost(0);
                setupPieChart(Collections.emptyMap());
            }
        });
    }

    private void loadUpcomingPayments() {
        long userId = getUserId();
        if (userId == -1) return;

        ApiService api = ApiClient.get(this).create(ApiService.class);
        api.getUpcomingSubscriptions(userId).enqueue(new Callback<UpcomingSubscriptionResponse>() {
            @Override public void onResponse(Call<UpcomingSubscriptionResponse> call, Response<UpcomingSubscriptionResponse> resp) {
                PaymentServiceAdapter ad = (PaymentServiceAdapter) paymentRecyclerView.getAdapter();
                if (resp.isSuccessful() && resp.body() != null && resp.body().getUpcomingPayments() != null) {
                    ad.updateData(resp.body().getUpcomingPayments());
                } else {
                    ad.updateData(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<UpcomingSubscriptionResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                PaymentServiceAdapter ad = (PaymentServiceAdapter) paymentRecyclerView.getAdapter();
                ad.updateData(new ArrayList<>());
            }
        });
    }

    private void loadSubscribedServices() {
        long userId = getUserId();
        if (userId == -1) return;

        ApiService api = ApiClient.get(this).create(ApiService.class);
        api.getSubscriptions(userId, 0, 50).enqueue(new Callback<PagedList<SubscriptionListItemDto>>() {
            @Override public void onResponse(Call<PagedList<SubscriptionListItemDto>> call,
                                             Response<PagedList<SubscriptionListItemDto>> resp) {
                SubscribedServiceAdapter ad = (SubscribedServiceAdapter) subscribedServicesRecyclerView.getAdapter();
                if (resp.isSuccessful() && resp.body() != null && resp.body().content != null) {
                    List<SubscribedService> viewList = new ArrayList<>();
                    for (SubscriptionListItemDto dto : resp.body().content) {
                        viewList.add(new SubscribedService(dto.serviceName, dto.logoUrl));
                    }
                    ad.updateData(viewList);
                } else {
                    ad.updateData(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<PagedList<SubscriptionListItemDto>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                SubscribedServiceAdapter ad = (SubscribedServiceAdapter) subscribedServicesRecyclerView.getAdapter();
                ad.updateData(new ArrayList<>());
            }
        });
    }

    private void updateMonthlyCost(int totalCost) {
        totalCostText.setText(String.format("%,d원", totalCost));
    }

    private void setupPieChart(@Nullable Map<String, Number> chartData) {
        if (chartData == null || chartData.isEmpty()) {
            pieChart.clear();
            pieChart.getDescription().setEnabled(false);
            pieChart.setNoDataText("표시할 데이터가 없습니다.");
            if (emptyChartView != null) {
                emptyChartView.setVisibility(View.VISIBLE);
                pieChart.setVisibility(View.INVISIBLE);
            }
            return;
        } else {
            if (emptyChartView != null) {
                emptyChartView.setVisibility(View.GONE);
                pieChart.setVisibility(View.VISIBLE);
            }
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Number> entry : chartData.entrySet()) {
            String label = entry.getKey() != null ? entry.getKey() : "";
            float value = entry.getValue() != null ? entry.getValue().floatValue() : 0f;
            if (value > 0f) entries.add(new PieEntry(value, label));
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.getDescription().setEnabled(false);
            pieChart.setNoDataText("표시할 데이터가 없습니다.");
            if (emptyChartView != null) {
                emptyChartView.setVisibility(View.VISIBLE);
                pieChart.setVisibility(View.INVISIBLE);
            }
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        int[] colors = new int[]{
                Color.parseColor("#F44336"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#9C27B0")
        };
        dataSet.setColors(colors);
        dataSet.setValueTextSize(0f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.invalidate();

        pieChart.setOnChartValueSelectedListener(this);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (!(e instanceof PieEntry)) return;
        PieEntry entry = (PieEntry) e;
        String platformName = entry.getLabel() != null ? entry.getLabel() : "";
        float cost = entry.getValue();
        String formattedCost = String.format("%,.0f원", cost);

        Toast toast = Toast.makeText(this, platformName + ": " + formattedCost, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 150);
        toast.show();
    }

    @Override
    public void onNothingSelected() {}

    private long getUserId() {
        SharedPreferences sp = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        return sp.getLong("user_id", -1L);
    }
}