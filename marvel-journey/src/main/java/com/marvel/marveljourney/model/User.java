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
    private String resetToken;
    private LoginAttempts loginAttempts;
    private List<String> roles;
    private List<Metadata> metadata;
    private MfaData mfa;
    private int failedLoginAttempts;
    private Instant lockoutEndTime;
    private VerificationCode verificationCode;
    
    @Data
    public static class LoginAttempts {
        private int count;
        private Instant lastAttemptAt;
    }

    @Data
    public static class MfaData {
        private String secret;
        private boolean enabled;
    }

    @Data
    public static class VerificationCode {
        private boolean emailIsVerified;
        private String code;
        private Instant createdAt;
    }

    @Data
    public static class Metadata {
        private String device;
        private String ipAddress;
        private String userAgent;
        private String refreshTokenHash;
        private Instant refreshTokenExpiryDate;
        private Instant lastLoginAt;
    }
}