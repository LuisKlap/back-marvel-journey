package com.marvel.marveljourney.exception;

public class InvalidIssuerOrAudienceException extends RuntimeException {
    public InvalidIssuerOrAudienceException(String message) {
        super(message);
    }
}