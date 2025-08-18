package com.example.subforest.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UpcomingSubscriptionResponse {
    @SerializedName("content")
    private List<SubscriptionListItemDto> content;
    @SerializedName("totalElements")
    private int totalElements;
    @SerializedName("totalPages")
    private int totalPages;

    public List<SubscriptionListItemDto> getContent() {
        return content;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }
}