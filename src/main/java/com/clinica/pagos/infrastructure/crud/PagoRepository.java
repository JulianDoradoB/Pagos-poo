package com.clinica.pagos.infrastructure.crud;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.clinica.pagos.infrastructure.entity.Pago;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByCitaId(Long citaId);
    List<Pago> findByEstado(String estado);
}