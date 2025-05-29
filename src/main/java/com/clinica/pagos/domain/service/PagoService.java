package com.clinica.pagos.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clinica.pagos.domain.dto.PagoDTO;
import com.clinica.pagos.domain.repository.IPago;
import com.clinica.pagos.infrastructure.client.CitaClient;
import com.clinica.pagos.infrastructure.client.NotificacionesFeignClient;
import com.clinica.pagos.infrastructure.client.dto.CitaDTO;
import com.clinica.pagos.infrastructure.client.dto.NotificacionDTO;

import feign.FeignException;

@Service
public class PagoService {

    @Autowired
    private IPago repo;

    @Autowired
    private CitaClient citaClient;

    @Autowired
    private NotificacionesFeignClient notificacionesFeignClient;

    public List<PagoDTO> obtenerTodo() {
        List<PagoDTO> pagos = repo.getAll();
        pagos.forEach(this::enriquecerPago);
        return pagos;
    }

    public Optional<PagoDTO> obtenerPorId(Long id) {
        Optional<PagoDTO> pago = repo.getById(id);
        pago.ifPresent(this::enriquecerPago);
        return pago;
    }

    public PagoDTO guardar(PagoDTO dto) {
        CitaDTO cita = null;
        try {
            cita = citaClient.getCitaById(dto.getCitaId());
            if (cita == null) {
                throw new RuntimeException("La cita con ID " + dto.getCitaId() + " no fue encontrada (respuesta nula).");
            }
        } catch (FeignException.NotFound e) {
            System.err.println("Error: Cita con ID " + dto.getCitaId() + " no existe.");
            throw new RuntimeException("La cita no existe", e);
        } catch (FeignException e) {
            System.err.println("Error al comunicarse con el servicio de citas (ID: " + dto.getCitaId() + "): " + e.getMessage());
            throw new RuntimeException("Error al comunicarse con el servicio de citas: " + e.getMessage(), e);
        }

        if (dto.getFechaPago() == null) {
            dto.setFechaPago(LocalDateTime.now());
        }

        if (dto.getEstado() == null) {
            dto.setEstado("PENDIENTE"); // Establece un estado inicial por defecto si no viene
        }

        // No hay estado previo al guardar, así que solo usamos el estado actual
        PagoDTO pagoGuardado = repo.save(dto);
        enriquecerPago(pagoGuardado);

        // Intenta enviar notificación si el estado es COMPLETADO después de guardar
        if ("COMPLETADO".equals(pagoGuardado.getEstado())) {
            enviarNotificacionPago(pagoGuardado, "COMPLETADO", cita);
        }

        return pagoGuardado;
    }

    public PagoDTO actualizar(Long id, PagoDTO dto) {
        // 1. Obtener el estado actual del pago antes de la actualización
        Optional<PagoDTO> pagoPrevioOpt = repo.getById(id);
        String estadoPrevio = pagoPrevioOpt.map(PagoDTO::getEstado).orElse(null);

        PagoDTO pagoActualizado = repo.update(id, dto);

        if (pagoActualizado != null) {
            enriquecerPago(pagoActualizado);

            // 2. Obtener la información de la cita para la notificación
            CitaDTO cita = null;
            try {
                cita = citaClient.getCitaById(pagoActualizado.getCitaId());
            } catch (FeignException e) {
                System.err.println("ADVERTENCIA: No se pudo obtener la cita para el pago " + id + " al actualizar. Notificación podría ser incompleta. Error: " + e.getMessage());
                // Podrías lanzar una excepción o manejarlo de otra forma si la cita es crítica
            }

            // 3. Lógica para enviar notificaciones basada en el CAMBIO de estado
            if (!pagoActualizado.getEstado().equalsIgnoreCase(estadoPrevio)) { // Solo si el estado ha cambiado
                if ("COMPLETADO".equals(pagoActualizado.getEstado())) {
                    enviarNotificacionPago(pagoActualizado, "COMPLETADO", cita);
                } else if ("ANULADO".equals(pagoActualizado.getEstado())) {
                    enviarNotificacionPago(pagoActualizado, "ANULADO", cita);
                }
                // Podrías añadir más condiciones aquí para otros cambios de estado
            } else {
                System.out.println("DEBUG: El estado del pago ID " + id + " no cambió (" + estadoPrevio + " -> " + pagoActualizado.getEstado() + "), no se envía notificación.");
            }
        }
        return pagoActualizado;
    }

    public boolean eliminar(Long id) {
        return repo.delete(id);
    }

    public List<PagoDTO> obtenerPorCita(Long citaId) {
        List<PagoDTO> pagos = repo.getByCitaId(citaId);
        pagos.forEach(this::enriquecerPago);
        return pagos;
    }

    public List<PagoDTO> obtenerPorEstado(String estado) {
        List<PagoDTO> pagos = repo.getByEstado(estado);
        pagos.forEach(this::enriquecerPago);
        return pagos;
    }

    private void enriquecerPago(PagoDTO pago) {
        if (pago.getCitaId() == null) {
            pago.setNombrePaciente("N/A");
            pago.setNombreMedico("N/A");
            return;
        }
        try {
            CitaDTO cita = citaClient.getCitaById(pago.getCitaId());
            if (cita != null) {
                pago.setNombrePaciente(cita.getNombrePaciente());
                pago.setNombreMedico(cita.getNombreMedico());
            } else {
                pago.setNombrePaciente("Paciente no encontrado (ID: " + pago.getCitaId() + ")");
                pago.setNombreMedico("Médico no encontrado (ID: " + pago.getCitaId() + ")");
            }
        } catch (FeignException.NotFound e) {
            System.err.println("Advertencia: Cita con ID " + pago.getCitaId() + " no encontrada al enriquecer pago.");
            pago.setNombrePaciente("Paciente no encontrado");
            pago.setNombreMedico("Médico no encontrado");
        } catch (FeignException e) {
            System.err.println("Error de comunicación con Citas al enriquecer pago " + pago.getId() + " para cita " + pago.getCitaId() + ": " + e.getMessage());
            pago.setNombrePaciente("Error de comunicación");
            pago.setNombreMedico("Error de comunicación");
        }
    }

    /**
     * Método auxiliar para enviar notificaciones de pago.
     * @param pago El PagoDTO que disparó la notificación.
     * @param tipoNotificacion El tipo de notificación (e.g., "COMPLETADO", "ANULADO").
     * @param cita La CitaDTO asociada al pago (puede ser null si no se pudo obtener).
     */
    private void enviarNotificacionPago(PagoDTO pago, String tipoNotificacion, CitaDTO cita) {
        System.out.println("DEBUG: El pago está " + tipoNotificacion + ", intentando enviar notificación.");
        try {
            String asunto = "";
            String mensaje = "";
            String eventType = "";

            if ("COMPLETADO".equals(tipoNotificacion)) {
                asunto = "Confirmación de Pago Completado para su Cita";
                mensaje = String.format("Estimado(a) %s,\n\nSu pago de %.2f USD para la cita con el Dr. %s ha sido completado exitosamente.\n\nReferencia de pago: %s",
                                         cita != null ? cita.getNombrePaciente() : "Paciente", pago.getMonto(), cita != null ? cita.getNombreMedico() : "Médico", pago.getReferencia());
                eventType = "PAGO_COMPLETADO";
            } else if ("ANULADO".equals(tipoNotificacion)) {
                asunto = "Notificación de Pago Anulado";
                mensaje = String.format("Estimado(a) %s,\n\nSu pago de %.2f USD para la cita con el Dr. %s ha sido ANULADO.\n\nReferencia de pago: %s",
                                         cita != null ? cita.getNombrePaciente() : "Paciente", pago.getMonto(), cita != null ? cita.getNombreMedico() : "Médico", pago.getReferencia());
                eventType = "PAGO_ANULADO";
            } else {
                System.out.println("ADVERTENCIA: Tipo de notificación no soportado: " + tipoNotificacion);
                return;
            }

            NotificacionDTO notificacion = new NotificacionDTO(
                cita != null ? cita.getPacienteId() : null, // ID del paciente
                eventType,
                asunto,
                mensaje,
                "EMAIL",
                "PagoService:" + pago.getId()
            );

            // Intentar obtener el email del paciente de la cita si está disponible
            if (cita != null && cita.getEmailPaciente() != null && !cita.getEmailPaciente().isEmpty()) {
                notificacion.setEmailDestinatario(cita.getEmailPaciente());
            } else {
                // Si no se tiene el email del paciente, usa el valor por defecto que manejará NotificacionesService
                // o considera lanzar una excepción si el email es obligatorio.
                System.err.println("ADVERTENCIA: No se pudo obtener el email del paciente para la cita ID " + pago.getCitaId() + ". Se enviará a 'nodisponible@example.com'.");
                notificacion.setEmailDestinatario("nodisponible@example.com"); // NotificacionesService ya maneja esto
            }

            notificacionesFeignClient.enviarNotificacion(notificacion);
            System.out.println("DEBUG: Notificación de pago enviada exitosamente a NotificacionesService para pago ID: " + pago.getId() + " (tipo: " + tipoNotificacion + ").");
        } catch (FeignException e) {
            System.err.println("ERROR Feign al enviar notificación de pago " + tipoNotificacion + " para ID " + pago.getId() + ": " + e.status() + " - " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERROR GENERAL al intentar enviar notificación de pago " + tipoNotificacion + " para ID " + pago.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}