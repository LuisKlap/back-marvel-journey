package com.marvel.marveljourney.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String email;
    private String passwordHash;
    private Instant termsAcceptedAt;
    private Boolean newsletterConsent;
    private Instant createdAt;
    private Instant updatedAt;
    private String status;
    private LoginAttempts loginAttempts;
    private List<String> roles;
    private Metadata metadata;
    private String mfaSecret;
    private boolean mfaEnabled;
    private int failedLoginAttempts;
    private Instant lockoutEndTime;
    private VerificationCode verificationCode;
    private boolean isTest;

    @Data
    public static class LoginAttempts {
        private int count;
        private Instant lastAttemptAt;
    }

    @Data
    public static class VerificationCode {
        private String email;
        private String code;
        private Instant createdAt;
    }

    @Data
    public static class Metadata {
        private Instant lastLoginAt;
        private String ipAddress;
        private String userAgent;
    }

    public boolean getIsTest() {
        return isTest;
    }

    public void setIsTest(boolean isTest) {
        this.isTest = isTest;
    }
}