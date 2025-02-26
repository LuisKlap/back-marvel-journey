package com.marvel.marveljourney.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PasswordResetRequest {
    private String email;
    private String newPassword;
    private String resetToken;
}
