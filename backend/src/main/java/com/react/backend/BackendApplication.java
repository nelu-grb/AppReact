package com.react.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.react.backend", "com.andesstay.reservations"})
@EnableJpaRepositories(basePackages = {"com.react.backend", "com.andesstay.reservations"})
@EntityScan(basePackages = {"com.react.backend", "com.andesstay.reservations"})
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}