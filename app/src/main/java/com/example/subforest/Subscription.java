package com.example.subforest;

public class Subscription {
    private String serviceName;
    private String period;
    private String price;
    private String date;
    private boolean isAutoPay;
    private boolean isShared;

    public Subscription(String serviceName, String period, String price, String date, boolean isAutoPay, boolean isShared) {
        this.serviceName = serviceName;
        this.period = period;
        this.price = price;
        this.date = date;
        this.isAutoPay = isAutoPay;
        this.isShared = isShared;
    }

    // Getter methods
    public String getServiceName() { return serviceName; }
    public String getPeriod() { return period; }
    public String getPrice() { return price; }
    public String getDate() { return date; }
    public boolean isAutoPay() { return isAutoPay; }
    public boolean isShared() { return isShared; }
}