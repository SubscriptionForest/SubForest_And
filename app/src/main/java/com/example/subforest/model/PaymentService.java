package com.example.subforest.model;

import com.google.gson.annotations.SerializedName;

public class PaymentService {

    @SerializedName("serviceName")
    private String serviceName;

    @SerializedName("amount")
    private int amount;

    // 서버: nextBillingDate -> 앱: nextPaymentDate
    @SerializedName("nextBillingDate")
    private String nextPaymentDate;

    // 서버: repeatCycleDays -> 앱: paymentCycle (문자열로 받고 있음)
    @SerializedName("repeatCycleDays")
    private String paymentCycle;

    // 서버: remainingDays -> 앱: daysLeft
    @SerializedName("remainingDays")
    private int daysLeft;

    // 서버: logoUrl -> 앱: serviceLogoUrl
    @SerializedName("logoUrl")
    private String serviceLogoUrl;

    public PaymentService(String serviceName, int amount, String nextPaymentDate,
                          String paymentCycle, int daysLeft, String serviceLogoUrl) {
        this.serviceName = serviceName;
        this.amount = amount;
        this.nextPaymentDate = nextPaymentDate;
        this.paymentCycle = paymentCycle;
        this.daysLeft = daysLeft;
        this.serviceLogoUrl = serviceLogoUrl;
    }

    public String getServiceName() { return serviceName; }
    public int getAmount() { return amount; }
    public String getNextPaymentDate() { return nextPaymentDate; }
    public String getPaymentCycle() { return paymentCycle; }
    public int getDaysLeft() { return daysLeft; }
    public String getServiceLogoUrl() { return serviceLogoUrl; }
}