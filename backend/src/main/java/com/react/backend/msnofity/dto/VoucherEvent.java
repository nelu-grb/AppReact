package com.react.backend.msnofity.dto;

public record VoucherEvent(
    String reservationId,
    String customerEmail,
    String voucherCode,
    Double amount
) {}
