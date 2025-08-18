package com.example.subforest.model;

public class RegisterRequest {
    private String email;
    private String name;
    private String password;

    public RegisterRequest(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
    }
}