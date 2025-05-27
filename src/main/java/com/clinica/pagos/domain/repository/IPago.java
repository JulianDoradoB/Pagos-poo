package com.clinica.pagos.domain.repository;

import java.util.List;
import java.util.Optional;

import com.clinica.pagos.domain.dto.PagoDTO;

public interface IPago {
    List<PagoDTO> getAll();
    Optional<PagoDTO> getById(Long id);
    PagoDTO save(PagoDTO dto);
    PagoDTO update(Long id, PagoDTO dto);
    boolean delete(Long id);
    
    // Métodos avanzados
    List<PagoDTO> getByCitaId(Long citaId);
    List<PagoDTO> getByEstado(String estado);
}