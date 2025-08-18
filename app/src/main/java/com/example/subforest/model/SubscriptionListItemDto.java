package com.example.subforest.model;

import com.google.gson.annotations.SerializedName;

public class SubscriptionListItemDto {
    @SerializedName("id")
    private long id;
    @SerializedName("serviceName")
    private String serviceName;
    @SerializedName("amount")
    private int amount;
    @SerializedName("nextPaymentDate")
    private String nextPaymentDate;
    @SerializedName("logoUrl")
    private String logoUrl;
    @SerializedName("daysLeft")
    private int daysLeft; // daysLeft 필드 추가

    public long getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getAmount() {
        return amount;
    }

    public String getNextPaymentDate() {
        return nextPaymentDate;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    // getDaysLeft() 메서드 추가
    public int getDaysLeft() {
        return daysLeft;
    }
}