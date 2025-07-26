package com.uthon.cocotomo.dto;

import lombok.Data;

@Data
public class EmailVerificationRequest {
    private String email;
    private String verificationCode;
}