package com.clinica.pagos.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagoDTO {
    private Long id;
    private Long citaId;
    private BigDecimal monto;
    private String metodoPago; // EFECTIVO, TARJETA, TRANSFERENCIA
    private LocalDateTime fechaPago;
    private String estado; // PENDIENTE, COMPLETADO, ANULADO
    private String referencia;
    
    // Datos adicionales (no se almacenan en la entidad Pago)
    private String nombrePaciente;
    private String nombreMedico;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getCitaId() { return citaId; }
    public void setCitaId(Long citaId) { this.citaId = citaId; }
    
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    
    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }
    
    public String getNombreMedico() { return nombreMedico; }
    public void setNombreMedico(String nombreMedico) { this.nombreMedico = nombreMedico; }
}