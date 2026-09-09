package com.andesstay.msreservations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.andesstay.msreservations")
@EntityScan(basePackages = "com.andesstay.msreservations")
public class MSReservationApplication{
    public static void main(String[] args) {
        SpringApplication.run(MSReservationApplication.class, args);
    }
}