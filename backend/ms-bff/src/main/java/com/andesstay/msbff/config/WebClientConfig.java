package com.andesstay.msbff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient reservationsWebClient(@Value("${downstream.reservations.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient catalogWebClient(@Value("${downstream.catalog.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}