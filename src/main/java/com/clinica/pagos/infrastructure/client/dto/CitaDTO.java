package com.clinica.pagos.infrastructure.client.dto;

import java.time.LocalDateTime;

public class CitaDTO {
    private Long id;
    private Long pacienteId;
    private Long medicoId;
    private LocalDateTime fechaHora;
    private String motivo;
    private String estado;
    private String observaciones;

    // Campos enriquecidos que vienen del microservicio de Citas
    private String nombrePaciente;
    private String nombreMedico;
    private String especialidadMedico;
    private String emailPaciente; // <--- ¡CAMBIO CLAVE: AÑADIDO ESTE CAMPO!

    // Constructores
    public CitaDTO() {}

    // Puedes añadir un constructor con todos los campos si lo necesitas,
    // pero para Feign, el constructor vacío y los setters son suficientes.


    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public Long getMedicoId() { return medicoId; }
    public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }

    public String getNombreMedico() { return nombreMedico; }
    public void setNombreMedico(String nombreMedico) { this.nombreMedico = nombreMedico; }

    public String getEspecialidadMedico() { return especialidadMedico; }
    public void setEspecialidadMedico(String especialidadMedico) { this.especialidadMedico = especialidadMedico; }

    // --- ¡NUEVO GETTER Y SETTER PARA EL EMAIL DEL PACIENTE! ---
    public String getEmailPaciente() {
        return emailPaciente;
    }

    public void setEmailPaciente(String emailPaciente) {
        this.emailPaciente = emailPaciente;
    }

    // Opcional: toString() para facilitar la depuración
    @Override
    public String toString() {
        return "CitaDTO{" +
                "id=" + id +
                ", pacienteId=" + pacienteId +
                ", medicoId=" + medicoId +
                ", fechaHora=" + fechaHora +
                ", motivo='" + motivo + '\'' +
                ", estado='" + estado + '\'' +
                ", observaciones='" + observaciones + '\'' +
                ", nombrePaciente='" + nombrePaciente + '\'' +
                ", nombreMedico='" + nombreMedico + '\'' +
                ", especialidadMedico='" + especialidadMedico + '\'' +
                ", emailPaciente='" + emailPaciente + '\'' + // Agregado al toString()
                '}';
    }
}