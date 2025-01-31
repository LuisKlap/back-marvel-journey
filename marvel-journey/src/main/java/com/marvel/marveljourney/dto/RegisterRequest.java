package com.marvel.marveljourney.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private boolean termsAccepted;
    private boolean updates;
    private String device;
    private String ipAddress;
    private String userAgent;
}