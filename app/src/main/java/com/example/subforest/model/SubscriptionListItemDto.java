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
}