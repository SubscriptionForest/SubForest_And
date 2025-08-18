// HomeActivity.java
package com.example.subforest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.subforest.adapter.PaymentServiceAdapter;
import com.example.subforest.adapter.SubscribedServiceAdapter;
import com.example.subforest.api.ApiClient;
import com.example.subforest.api.ApiService;
import com.example.subforest.model.DashboardSummaryResponse;
import com.example.subforest.model.PaymentService;
import com.example.subforest.model.SubscribedService;
import com.example.subforest.model.UpcomingSubscriptionResponse;
import com.example.subforest.model.SubscriptionsListResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private TextView greetingText, totalCostText;
    private PieChart pieChart;
    private RecyclerView subscribedServicesRecyclerView, paymentRecyclerView;
    private Button addSubscriptionBtn;
    private BottomNavigationView bottomNavigationView;
    private String userName; // 사용자 이름

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
                return true;
            } else if (id == R.id.nav_profile) {
                // MypageActivity로 이동
                return true;
            } else if (id == R.id.nav_home) {
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerViews() {
        // 구독 중인 서비스 RecyclerView (가로 스크롤)
        subscribedServicesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        subscribedServicesRecyclerView.setAdapter(new SubscribedServiceAdapter(new ArrayList<>()));

        // 결제 예정 서비스 RecyclerView (가로 스크롤)
        paymentRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        paymentRecyclerView.setAdapter(new PaymentServiceAdapter(new ArrayList<>()));
    }

    private void loadDashboardSummary() {
        // userId 가져오기
        long userId = getUserId();
        if (userId == -1) return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<DashboardSummaryResponse> call = apiService.getDashboardSummary(userId);

        call.enqueue(new Callback<DashboardSummaryResponse>() {
            @Override
            public void onResponse(Call<DashboardSummaryResponse> call, Response<DashboardSummaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardSummaryResponse summary = response.body();
                    updateMonthlyCost(summary.getTotalAmount());
                    setupPieChart(summary.getChartData());
                } else {
                    Toast.makeText(HomeActivity.this, "대시보드 데이터 로드 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DashboardSummaryResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUpcomingPayments() {
        long userId = getUserId();
        if (userId == -1) return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<UpcomingSubscriptionResponse> call = apiService.getUpcomingSubscriptions(userId);

        call.enqueue(new Callback<UpcomingSubscriptionResponse>() {
            @Override
            public void onResponse(Call<UpcomingSubscriptionResponse> call, Response<UpcomingSubscriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PaymentService> paymentServices = new ArrayList<>();
                    ((PaymentServiceAdapter) paymentRecyclerView.getAdapter()).updateData(paymentServices);
                } else {
                    Toast.makeText(HomeActivity.this, "결제 예정 서비스 로드 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpcomingSubscriptionResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSubscribedServices() {
        long userId = getUserId();
        if (userId == -1) return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<SubscriptionsListResponse> call = apiService.getSubscriptionsList(userId);

        call.enqueue(new Callback<SubscriptionsListResponse>() {
            @Override
            public void onResponse(Call<SubscriptionsListResponse> call, Response<SubscriptionsListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SubscribedService> subscribedServices = new ArrayList<>();
                    ((SubscribedServiceAdapter) subscribedServicesRecyclerView.getAdapter()).updateData(subscribedServices);
                } else {
                    Toast.makeText(HomeActivity.this, "구독 중인 서비스 로드 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SubscriptionsListResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMonthlyCost(int totalCost) {
        totalCostText.setText(String.format("%,d원", totalCost));
    }

    private void setupPieChart(Map<String, Integer> chartData) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        int[] colors = new int[]{
                Color.parseColor("#F44336"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#4CAF50")
        };
        int colorIndex = 0;
        for (Map.Entry<String, Integer> entry : chartData.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(0f);
        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setOrientation(Legend.LegendOrientation.VERTICAL);
        pieChart.invalidate();
    }

    // 사용자 ID를 가져오는 임시 메서드
    private long getUserId() {
        // 실제로는 로그인 후 SharedPreferences에 저장된 userId를 가져와야 함
        return 1L;
    }
}