package com.marvel.marveljourney.exception;

public enum ErrorCode {
    INVALID_CREDENTIALS("Invalid credentials."),
    ACCOUNT_LOCKED("Account locked. Try again later."),
    EMAIL_NOT_VERIFIED("Email not verified. Please verify your email before logging in."),
    INTERNAL_SERVER_ERROR("Internal server error"),
    USER_NOT_FOUND("User not found."),
    INVALID_VERIFICATION_CODE("Invalid verification code."),
    EMAIL_ALREADY_REGISTERED("Email already registered."),
    WEAK_PASSWORD("Weak password."),
    EXPIRED_VERIFICATION_CODE("Verification code expired."),
    INVALID_PASSWORD("Invalid password."),
    INVALID_RESET_TOKEN("Invalid reset token.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}