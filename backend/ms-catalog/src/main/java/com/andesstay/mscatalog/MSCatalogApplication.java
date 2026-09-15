package com.andesstay.mscatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.andesstay.mscatalog")
@EntityScan(basePackages = "com.andesstay.mscatalog")
public class MSCatalogApplication {
    public static void main(String[] args) {
        SpringApplication.run(MSCatalogApplication.class, args);
    }
}