package com.example.subforest.model;

    public class PaymentService {
        private String serviceName;
        private int amount;
        private String nextPaymentDate;
        private String paymentCycle;
        private int daysLeft;
        private String serviceLogoUrl;

        public PaymentService(String serviceName, int amount, String nextPaymentDate, String paymentCycle, int daysLeft, String serviceLogoUrl) {
            this.serviceName = serviceName;
            this.amount = amount;
            this.nextPaymentDate = nextPaymentDate;
            this.paymentCycle = paymentCycle;
            this.daysLeft = daysLeft;
            this.serviceLogoUrl = serviceLogoUrl;
        }

        // Getter and Setter methods
        public String getServiceName() { return serviceName; }
        public int getAmount() { return amount; }
        public String getNextPaymentDate() { return nextPaymentDate; }
        public String getPaymentCycle() { return paymentCycle; }
        public int getDaysLeft() { return daysLeft; }
        public String getServiceLogoUrl() { return serviceLogoUrl; }


    }