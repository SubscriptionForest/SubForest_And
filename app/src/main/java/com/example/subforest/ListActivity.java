package com.example.subforest;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SubscriptionAdapter adapter;
    Spinner sortSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        sortSpinner = findViewById(R.id.sortSpinner);
        recyclerView = findViewById(R.id.subscriptionRecyclerView);

        // Spinner 설정
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"결제일 순", "이름 순", "금액 순"});
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        // RecyclerView 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubscriptionAdapter(getDummyData());
        recyclerView.setAdapter(adapter);

        // 하단 네비게이션
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

    // 테스트용 더미 데이터
    private List<Subscription> getDummyData() {
        List<Subscription> list = new ArrayList<>();
        list.add(new Subscription("YouTube", "7/25 - 8/25", "5,900원", "8/25", true, false));
        list.add(new Subscription("Spotify", "5/29 - 8/29", "12,900원", "8/29", false, false));
        list.add(new Subscription("Netflix", "7/31 - 8/31", "2,300원 (공유)", "8/31", true, true));
        return list;
    }
}