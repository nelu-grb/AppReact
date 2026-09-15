package com.react.backend.msnofity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotifyApplication {

    public static void main(String[] args) {
        System.setProperty("server.port", "8084");
        SpringApplication.run(NotifyApplication.class, args);
    }
}