package com.example.subforest.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UpcomingSubscriptionResponse {
    @SerializedName("content")
    private List<PaymentService> upcomingPayments; // PaymentService로 타입 변경

    @SerializedName("totalElements")
    private int totalElements;

    @SerializedName("totalPages")
    private int totalPages;

    // 이 메서드명을 getUpcomingPayments()로 변경
    public List<PaymentService> getUpcomingPayments() {
        return upcomingPayments;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }
}