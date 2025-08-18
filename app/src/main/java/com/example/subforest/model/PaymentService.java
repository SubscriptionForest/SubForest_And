// package com.example.subforest.model;
package com.example.subforest.model;

public class PaymentService {
    private String serviceName;
    private int amount;
    private String nextPaymentDate;

    public PaymentService(String serviceName, int amount, String nextPaymentDate) {
        this.serviceName = serviceName;
        this.amount = amount;
        this.nextPaymentDate = nextPaymentDate;
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