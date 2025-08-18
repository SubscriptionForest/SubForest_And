package com.example.subforest.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class DashboardSummaryResponse {
    @SerializedName("totalAmount")
    private int totalAmount;
    @SerializedName("subscriptionCount")
    private int subscriptionCount;
    @SerializedName("chartData")
    private Map<String, Integer> chartData;

    public int getTotalAmount() {
        return totalAmount;
    }

    public int getSubscriptionCount() {
        return subscriptionCount;
    }

    public Map<String, Integer> getChartData() {
        return chartData;
    }
}