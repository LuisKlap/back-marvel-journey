package com.marvel.marveljourney.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
public class VerificationRequest {
    private String email;
    private String code;
}