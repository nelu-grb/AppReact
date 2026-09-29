package com.andesstay.msreservations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentInitResponseDto {
    private String url;
    private String token;
}