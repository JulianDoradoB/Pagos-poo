package com.clinica.pagos.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.clinica.pagos.infrastructure.client.dto.CitaDTO;

@FeignClient(name = "citas-service", url ="http://localhost:8097")
public interface CitaClient {
    
    @GetMapping("/citas/{id}")
    CitaDTO getCitaById(@PathVariable("id") Long id);
}