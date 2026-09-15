package com.react.backend.msaudit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuditApplication {

    public static void main(String[] args) {
        System.setProperty("server.port", "8083");
        SpringApplication.run(AuditApplication.class, args);
    }
}