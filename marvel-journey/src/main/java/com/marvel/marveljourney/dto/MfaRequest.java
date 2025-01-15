package com.marvel.marveljourney.dto;

public class MfaRequest {
    private String email;

    public MfaRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
