package com.andesstay.msreservations.service;

import cl.transbank.common.IntegrationApiKeys;
import cl.transbank.common.IntegrationCommerceCodes;
import cl.transbank.common.IntegrationType;
import cl.transbank.webpay.common.WebpayOptions;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCommitResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WebpayService {

    @Value("${transbank.return-url}")
    private String returnUrl;

    private WebpayPlus.Transaction transaction;

    @PostConstruct
    public void init() {
        this.transaction = new WebpayPlus.Transaction(new WebpayOptions(
                IntegrationCommerceCodes.WEBPAY_PLUS,
                IntegrationApiKeys.WEBPAY_PLUS,
                IntegrationType.TEST
        ));
    }

    public WebpayPlusTransactionCreateResponse createTransaction(Long reservationId, String sessionId, double amount) throws Exception {
        String buyOrder = "ORD-" + reservationId;
        return transaction.create(buyOrder, sessionId, amount, returnUrl);
    }

    public WebpayPlusTransactionCommitResponse commitTransaction(String token) throws Exception {
        return transaction.commit(token);
    }
}