// src/main/java/com/clinica/pagos/infrastructure/client/NotificacionesFeignClient.java
package com.clinica.pagos.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.clinica.pagos.infrastructure.client.dto.NotificacionDTO; // Asegúrate de que la ruta sea correcta

// Definimos el FeignClient para el microservicio de Notificaciones
// 'name' es un nombre lógico para el cliente
// 'url' es la URL base del microservicio de Notificaciones. ¡Aquí ponemos el puerto 8091!
@FeignClient(name = "notificaciones-service", url = "http://localhost:8090")
public interface NotificacionesFeignClient {

    // Este método se mapea al endpoint POST /notificaciones/enviar del microservicio de Notificaciones
    @PostMapping("/notificaciones/enviar")
    void enviarNotificacion(@RequestBody NotificacionDTO dto);
}