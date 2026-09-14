package com.react.backend.msnofity.service;

import com.react.backend.msnofity.dto.VoucherEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VoucherService {

    private static final Logger log = LoggerFactory.getLogger(VoucherService.class);

    public void generateVoucher(VoucherEvent event) {
        log.info("Generando VOUCHER -> Reserva: {} | Cliente: {} | Código: {}", 
                event.reservationId(), event.customerEmail(), event.voucherCode());
    }
}
