package com.example.subforest.model;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class DashboardSummaryResponse {

    @SerializedName("totalMonthlySpend")
    private Number totalMonthlySpend;

    @SerializedName("activeCount")
    private Integer activeCount;

    @SerializedName("byCategory")
    private List<PieSlice> byCategory;

    @SerializedName("upcomingPayments")
    private List<DailySum> upcomingPayments;

    // --- 안전한 getter ---
    public int getTotalMonthlySpend() {
        return totalMonthlySpend == null ? 0 : totalMonthlySpend.intValue();
    }

    public int getActiveCount() {
        return activeCount == null ? 0 : activeCount;
    }

    public List<PieSlice> getByCategory() {
        return byCategory == null ? Collections.emptyList() : byCategory;
    }

    public List<DailySum> getUpcomingPayments() {
        return upcomingPayments == null ? Collections.emptyList() : upcomingPayments;
    }

    // 차트용 Map으로 변환: 카테고리 → 금액
    public Map<String, Number> toChartMap() {
        List<PieSlice> list = getByCategory();
        Map<String, Number> out = new LinkedHashMap<>();
        for (PieSlice s : list) {
            if (s != null && s.category != null && s.amount != null) {
                out.put(s.category, s.amount);
            }
        }
        return out;
    }

    public static class PieSlice {
        @SerializedName("category")
        public String category;
        @SerializedName("amount")
        public Number amount;
    }

    public static class DailySum {
        @SerializedName("date")
        public String date;   // yyyy-MM-dd
        @SerializedName("amount")
        public Number amount;
    }
}