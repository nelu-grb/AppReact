package com.andesstay.msreservations.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebpayService {

    private final RestTemplate restTemplate = new RestTemplate();

    // Si transbank.return-url no está en application.yml, usa esta URL de fallback por defecto
    @Value("${transbank.return-url:http://localhost:5173/payment/result}")
    private String returnUrl;

    private static final String WEBPAY_URL = "https://webpay3gint.transbank.cl/rswebpaytransaction/api/webpay/v1.2/transactions";
    private static final String COMMERCE_CODE = "597055555532";
    private static final String API_KEY = "579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C";

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Tbk-Api-Key-Id", COMMERCE_CODE);
        headers.set("Tbk-Api-Key-Secret", API_KEY);
        return headers;
    }

    public WebpayCreateResponse createTransaction(Long reservationId, String sessionId, BigDecimal amount) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("buy_order", "ORD-" + reservationId);
        requestBody.put("session_id", sessionId);
        requestBody.put("amount", amount.intValue());
        requestBody.put("return_url", returnUrl);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, createHeaders());
        ResponseEntity<WebpayCreateResponse> response = restTemplate.postForEntity(WEBPAY_URL, entity, WebpayCreateResponse.class);

        return response.getBody();
    }

    public WebpayCommitResponse commitTransaction(String token) {
        String url = WEBPAY_URL + "/" + token;
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());

        ResponseEntity<WebpayCommitResponse> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                entity,
                WebpayCommitResponse.class
        );

        return response.getBody();
    }

    @Data
    public static class WebpayCreateResponse {
        private String url;
        private String token;
    }

    @Data
    public static class WebpayCommitResponse {
        @JsonProperty("response_code")
        private int responseCode;
        private String status;
        @JsonProperty("buy_order")
        private String buyOrder;
        @JsonProperty("authorization_code")
        private String authorizationCode;
        private BigDecimal amount;
    }
}