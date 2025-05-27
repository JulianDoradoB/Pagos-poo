package com.clinica.pagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PagosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagosApplication.class, args);
    }
}