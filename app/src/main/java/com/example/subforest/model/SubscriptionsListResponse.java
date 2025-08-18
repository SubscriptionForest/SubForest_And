package com.example.subforest.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SubscriptionsListResponse {
    @SerializedName("content")
    private List<SubscriptionListItemDto> content;
    @SerializedName("totalElements")
    private int totalElements;
    @SerializedName("totalPages")
    private int totalPages;

    // 이 메서드명을 getSubscriptions()로 변경
    public List<SubscriptionListItemDto> getSubscriptions() {
        return content;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }
}