package com.example.subforest;

import android.content.Intent;
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

// 통신을 위한 라이브러리(Retrofit, Volley 등)를 가정하고 작성합니다.
import com.example.subforest.api.ApiClient;
import com.example.subforest.api.ApiService;
import com.example.subforest.model.SubscriptionResponse; // 구독 목록 응답 데이터 모델
import com.example.subforest.adapter.SubscribedServiceAdapter;
import com.example.subforest.adapter.PaymentServiceAdapter;
import com.example.subforest.model.SubscribedService;
import com.example.subforest.model.PaymentService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private TextView greetingText, totalCostText;
    private PieChart pieChart;
    private Button addSubscriptionBtn;
    private BottomNavigationView bottomNavigationView;
    private RecyclerView subscribedServicesRecyclerView;
    private RecyclerView paymentRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // UI 요소 초기화
        greetingText = findViewById(R.id.greetingText);
        totalCostText = findViewById(R.id.monthlyCostValue); // totalCostText 대신 monthlyCostValue로 수정
        pieChart = findViewById(R.id.pieChart);
        addSubscriptionBtn = findViewById(R.id.addSubscriptionButton); // addSubscriptionBtn 대신 addSubscriptionButton로 수정
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        subscribedServicesRecyclerView = findViewById(R.id.subscribedServicesRecyclerView);
        paymentRecyclerView = findViewById(R.id.paymentRecyclerView);

        // RecyclerView 설정
        setupRecyclerViews();

        // 백엔드 데이터 로드 및 UI 업데이트
        loadSubscriptionData();

        addSubscriptionBtn.setOnClickListener(v -> {
            // "구독 등록하기" 버튼 클릭 시 이동할 Activity 설정
            Intent intent = new Intent(HomeActivity.this, AddSubscriptionActivity.class);
            startActivity(intent);
        });

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
        // 구독 중인 서비스 RecyclerView
        subscribedServicesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        subscribedServicesRecyclerView.setAdapter(new SubscribedServiceAdapter(new ArrayList<>()));

        // 결제 예정 서비스 RecyclerView
        paymentRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        paymentRecyclerView.setAdapter(new PaymentServiceAdapter(new ArrayList<>()));
    }

    private void loadSubscriptionData() {
        // 사용자 ID를 가져오는 로직 (예: SharedPreferences)
        long userId = 1; // 임시 사용자 ID

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<SubscriptionResponse> call = apiService.getSubscriptions(userId);

        call.enqueue(new Callback<SubscriptionResponse>() {
            @Override
            public void onResponse(Call<SubscriptionResponse> call, Response<SubscriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SubscriptionResponse subscriptionResponse = response.body();
                    List<Subscription> subscriptions = subscriptionResponse.getSubscriptions();

                    // 구독 비용 계산 및 UI 업데이트
                    int totalCost = calculateTotalCost(subscriptions);
                    updateMonthlyCost(totalCost);

                    // 파이 차트 데이터 생성 및 업데이트
                    List<PieEntry> pieEntries = createPieChartEntries(subscriptions);
                    setupPieChart(pieEntries);

                    // 구독 서비스 목록 업데이트
                    List<SubscribedService> subscribedServices = createSubscribedServiceList(subscriptions);
                    ((SubscribedServiceAdapter) subscribedServicesRecyclerView.getAdapter()).updateData(subscribedServices);

                    // 결제 예정 서비스 목록 업데이트
                    List<PaymentService> paymentServices = createPaymentServiceList(subscriptions);
                    ((PaymentServiceAdapter) paymentRecyclerView.getAdapter()).updateData(paymentServices);

                } else {
                    Toast.makeText(HomeActivity.this, "데이터 로드 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SubscriptionResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int calculateTotalCost(List<Subscription> subscriptions) {
        int total = 0;
        for (Subscription sub : subscriptions) {
            total += sub.getAmount();
        }
        return total;
    }

    private void updateMonthlyCost(int totalCost) {
        totalCostText.setText(String.format("%,d원", totalCost));
    }

    private List<PieEntry> createPieChartEntries(List<Subscription> subscriptions) {
        List<PieEntry> entries = new ArrayList<>();
        for (Subscription sub : subscriptions) {
            entries.add(new PieEntry(sub.getAmount(), sub.getService() != null ? sub.getService().getName() : sub.getCustomService().getName()));
        }
        return entries;
    }

    private void setupPieChart(List<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        // 색상 동적 설정 로직 추가 필요
        dataSet.setColors(new int[]{
                Color.parseColor("#F44336"), Color.parseColor("#2196F3"), Color.parseColor("#4CAF50")
        });

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

    private List<SubscribedService> createSubscribedServiceList(List<Subscription> subscriptions) {
        List<SubscribedService> services = new ArrayList<>();
        for (Subscription sub : subscriptions) {
            // 로고 URL을 통해 리소스 ID를 가져오는 로직 필요
            int logoResId = getLogoResId(sub.getService() != null ? sub.getService().getLogoUrl() : sub.getCustomService().getLogoUrl());
            services.add(new SubscribedService(
                    sub.getService() != null ? sub.getService().getName() : sub.getCustomService().getName(),
                    logoResId
            ));
        }
        return services;
    }

    private List<PaymentService> createPaymentServiceList(List<Subscription> subscriptions) {
        List<PaymentService> paymentServices = new ArrayList<>();
        for (Subscription sub : subscriptions) {
            // 로고, 금액, 색상 정보 등을 백엔드 데이터에서 가져와서 PaymentService 객체 생성
            int logoResId = getLogoResId(sub.getService() != null ? sub.getService().getLogoUrl() : sub.getCustomService().getLogoUrl());
            int backgroundColor = getBackgroundColor(sub.getService() != null ? sub.getService().getName() : sub.getCustomService().getName());
            int borderColor = getBorderColor(sub.getService() != null ? sub.getService().getName() : sub.getCustomService().getName());

            paymentServices.add(new PaymentService(
                    sub.getService() != null ? sub.getService().getName() : sub.getCustomService().getName(),
                    String.format("%,d원 / %s", sub.getAmount(), getCycleString(sub.getRepeatCycleDays())),
                    logoResId,
                    backgroundColor,
                    borderColor
            ));
        }
        return paymentServices;
    }

    private int getLogoResId(String logoUrl) {
        // 로고 URL에 맞는 리소스 ID를 찾는 로직
        // 예시: "https://.../netflix.png" -> R.drawable.logo_netflix
        return R.drawable.logo_netflix; // 임시 값
    }

    private int getBackgroundColor(String serviceName) {
        // 서비스 이름에 따른 배경색 반환 로직
        return ContextCompat.getColor(this, R.color.red_background); // 임시 값
    }

    private int getBorderColor(String serviceName) {
        // 서비스 이름에 따른 테두리색 반환 로직
        return ContextCompat.getColor(this, R.color.red_stroke); // 임시 값
    }

    private String getCycleString(int repeatCycleDays) {
        // 결제 주기에 따른 문자열 반환 로직
        if (repeatCycleDays == 30) return "한 달";
        return repeatCycleDays + "일";
    }
}