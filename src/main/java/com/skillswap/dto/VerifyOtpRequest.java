package com.skillswap.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String otp;
}