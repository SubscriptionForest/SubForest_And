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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import com.example.subforest.adapter.PaymentServiceAdapter;
import com.example.subforest.adapter.SubscribedServiceAdapter;
import com.example.subforest.model.DashboardSummaryResponse;
import com.example.subforest.model.PaymentService;
import com.example.subforest.model.SubscribedService;
import com.example.subforest.model.UpcomingSubscriptionResponse;
import com.example.subforest.network.ApiClient;
import com.example.subforest.network.ApiService;
import com.example.subforest.network.ApiDtos.PagedList;
import com.example.subforest.network.ApiDtos.SubscriptionListItemDto;
import com.example.subforest.ui.AddActivity;
import com.example.subforest.ui.ListActivity;
import com.example.subforest.ui.MypageActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener; // 리스너 추가
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
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

        ApiService api = ApiClient.get(this).create(ApiService.class);
        api.getDashboardSummary(userId).enqueue(new Callback<DashboardSummaryResponse>() {
            @Override public void onResponse(Call<DashboardSummaryResponse> call, Response<DashboardSummaryResponse> resp) {
                if (resp.isSuccessful() && resp.body()!=null) {
                    DashboardSummaryResponse s = resp.body();
                    updateMonthlyCost(s.getTotalAmount());
                    setupPieChart(s.getChartData());
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

        ApiService api = ApiClient.get(this).create(ApiService.class);
        api.getUpcomingSubscriptions(userId).enqueue(new Callback<UpcomingSubscriptionResponse>() {
            @Override public void onResponse(Call<UpcomingSubscriptionResponse> call, Response<UpcomingSubscriptionResponse> resp) {
                if (resp.isSuccessful() && resp.body()!=null) {
                    List<PaymentService> list = resp.body().getUpcomingPayments();
                    PaymentServiceAdapter ad = (PaymentServiceAdapter) paymentRecyclerView.getAdapter();
                    if (ad != null) ad.updateData(list != null ? list : new ArrayList<>());
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

        ApiService api = ApiClient.get(this).create(ApiService.class);
        api.getSubscriptions(userId, 0, 50).enqueue(new Callback<PagedList<SubscriptionListItemDto>>() {
            @Override public void onResponse(Call<PagedList<SubscriptionListItemDto>> call,
                                             Response<PagedList<SubscriptionListItemDto>> resp) {
                if (resp.isSuccessful() && resp.body()!=null && resp.body().content != null) {
                    List<SubscriptionListItemDto> dtoList = resp.body().content;
                    List<SubscribedService> viewList = new ArrayList<>();
                    for (SubscriptionListItemDto dto : dtoList) {
                        // 어댑터가 요구하는 간단 모델로 매핑
                        viewList.add(new SubscribedService(dto.serviceName, dto.logoUrl));
                    }
                    SubscribedServiceAdapter ad = (SubscribedServiceAdapter) subscribedServicesRecyclerView.getAdapter();
                    if (ad != null) ad.updateData(viewList);
                } else {
                    Toast.makeText(HomeActivity.this, "구독 중인 서비스 로드 실패", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<PagedList<SubscriptionListItemDto>> call, Throwable t) {
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
        pieChart.getLegend().setEnabled(false); // 범례 제거
        pieChart.invalidate();
        pieChart.setOnChartValueSelectedListener(this); // 클릭 리스너 추가
    }

    // PieChart 리스너 메서드 구현

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        // PieEntry 객체에서 플랫폼 이름과 금액 정보를 가져옵니다.
        PieEntry entry = (PieEntry) e;
        String platformName = entry.getLabel(); // 플랫폼 이름
        float cost = entry.getValue(); // 금액

        // 금액을 정수 형식으로 변환하고 쉼표를 추가합니다.
        String formattedCost = String.format("%,.0f원", cost);

        // Toast에 표시할 최종 메시지를 만듭니다.
        String message = platformName + ": " + formattedCost;

        // Toast를 만들고 위치를 설정한 후 보여줍니다.
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 150);
        toast.show();
    }

    @Override
    public void onNothingSelected() {
        // 아무것도 선택되지 않았을 때의 동작
    }

    // 사용자 ID를 가져오는 임시 메서드
    private long getUserId() {
        // 실제로는 로그인 후 SharedPreferences에 저장된 userId를 가져와야 함
        return 1L;
    }
}