package com.example.subforest.model;

import com.google.gson.annotations.SerializedName;

public class SubscriptionListItemDto {
    @SerializedName("id")
    private long id;
    @SerializedName("serviceName")
    private String serviceName;
    @SerializedName("amount")
    private int amount;
    @SerializedName("nextBillingDate")
    private String nextBillingDate;
    @SerializedName("logoUrl")
    private String logoUrl;
    @SerializedName("remainingDays")
    private int remainingDays;

    public long getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getAmount() {
        return amount;
    }

    public String getNextBillingDate() {
        return nextBillingDate;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public int getRemainingDays() {
        return remainingDays;
    }
}