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
import com.clinica.pagos.infrastructure.client.dto.NotificacionDTO; // ¡Correcto! Ahora es singular

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
            dto.setEstado("COMPLETADO");
        }

        PagoDTO pagoGuardado = repo.save(dto);
        enriquecerPago(pagoGuardado);

        System.out.println("DEBUG: Estado del pago guardado: " + pagoGuardado.getEstado());
        if ("COMPLETADO".equals(pagoGuardado.getEstado())) {
            System.out.println("DEBUG: El pago está COMPLETADO, intentando enviar notificación.");
            try {
                String asunto = "Confirmación de Pago Completado para su Cita";
                String mensaje = String.format("Estimado(a) %s,\n\nSu pago de %.2f USD para la cita con el Dr. %s ha sido completado exitosamente.\n\nReferencia de pago: %s",
                                                cita.getNombrePaciente(), pagoGuardado.getMonto(), cita.getNombreMedico(), pagoGuardado.getReferencia());

                // ¡Aquí está la llamada al constructor que te daba error!
                // Es correcta y coincide con el constructor de 6 parámetros en tu NotificacionDTO del lado de Pagos.
                NotificacionDTO notificacion = new NotificacionDTO(
                    cita.getPacienteId(),
                    "PAGO_COMPLETADO",
                    asunto,
                    mensaje,
                    "EMAIL",
                    "PagoService:" + pagoGuardado.getId()
                );

                notificacionesFeignClient.enviarNotificacion(notificacion);
                System.out.println("DEBUG: Notificación de pago enviada exitosamente a NotificacionesService.");
            } catch (FeignException e) {
                System.err.println("ERROR Feign al enviar notificación de pago completado: " + e.status() + " - " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("ERROR GENERAL al intentar enviar notificación de pago completado: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("DEBUG: El estado del pago no es COMPLETADO, no se envía notificación.");
        }

        return pagoGuardado;
    }

    public PagoDTO actualizar(Long id, PagoDTO dto) {
        PagoDTO pagoActualizado = repo.update(id, dto);
        if (pagoActualizado != null) {
            enriquecerPago(pagoActualizado);

            if ("ANULADO".equals(pagoActualizado.getEstado())) {
                System.out.println("DEBUG: El pago está ANULADO, intentando enviar notificación.");
                try {
                    CitaDTO cita = citaClient.getCitaById(pagoActualizado.getCitaId());
                    String asunto = "Notificación de Pago Anulado";
                    String mensaje = String.format("Estimado(a) %s,\n\nSu pago de %.2f USD para la cita con el Dr. %s ha sido ANULADO.\n\nReferencia de pago: %s",
                                                    cita.getNombrePaciente(), pagoActualizado.getMonto(), cita.getNombreMedico(), pagoActualizado.getReferencia());

                    NotificacionDTO notificacion = new NotificacionDTO(
                        cita.getPacienteId(),
                        "PAGO_ANULADO",
                        asunto,
                        mensaje,
                        "EMAIL",
                        "PagoService:" + pagoActualizado.getId()
                    );
                    notificacionesFeignClient.enviarNotificacion(notificacion);
                    System.out.println("DEBUG: Notificación de pago anulado enviada exitosamente.");
                } catch (FeignException e) {
                    System.err.println("ERROR Feign al enviar notificación de pago anulado: " + e.status() + " - " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    System.err.println("ERROR GENERAL al intentar enviar notificación de pago anulado: " + e.getMessage());
                    e.printStackTrace();
                }
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
}