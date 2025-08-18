// package com.example.subforest.model;
package com.example.subforest.model;

public class SubscribedService {
    private String name;
    private String logoUrl;

    public SubscribedService(String name, String logoUrl) {
        this.name = name;
        this.logoUrl = logoUrl;
    }

    public String getName() {
        return name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }
}