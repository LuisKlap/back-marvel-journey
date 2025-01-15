package com.marvel.marveljourney.dto;

public class LoginRequest {
    private String email;
    private String password;
    private String ipAddress;
    private String userAgent;

    public LoginRequest(String email, String password, String ipAddress, String userAgent) {
        this.email = email;
        this.password = password;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
