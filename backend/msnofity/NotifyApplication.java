package com.react.backend.msnofity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotifyApplication {

    public static void main(String[] args) {
        // Sobrescribe el puerto del application.properties solo para este microservicio
        System.setProperty("server.port", "8082"); 
        SpringApplication.run(NotifyApplication.class, args);
    }
}