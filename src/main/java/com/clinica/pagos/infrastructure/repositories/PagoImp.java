package com.clinica.pagos.infrastructure.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.clinica.pagos.domain.dto.PagoDTO;
import com.clinica.pagos.domain.repository.IPago;
import com.clinica.pagos.infrastructure.crud.PagoRepository;
import com.clinica.pagos.infrastructure.entity.Pago;
import com.clinica.pagos.infrastructure.mapper.PagoMapper;

@Repository
public class PagoImp implements IPago {

    @Autowired
    private PagoRepository repo;

    @Autowired
    private PagoMapper mapper;

    @Override
    public List<PagoDTO> getAll() {
        return mapper.toPagosDTO(repo.findAll());
    }

    @Override
    public Optional<PagoDTO> getById(Long id) {
        return repo.findById(id).map(mapper::toPagoDTO);
    }

    @Override
    public PagoDTO save(PagoDTO dto) {
        Pago ent = mapper.toPago(dto);
        return mapper.toPagoDTO(repo.save(ent));
    }

    @Override
    public PagoDTO update(Long id, PagoDTO dto) {
        return repo.findById(id).map(existingEntity -> {
            Pago ent = mapper.toPago(dto);
            ent.setId(id);
            return mapper.toPagoDTO(repo.save(ent));
        }).orElse(null);
    }

    @Override
    public boolean delete(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<PagoDTO> getByCitaId(Long citaId) {
        return mapper.toPagosDTO(repo.findByCitaId(citaId));
    }

    @Override
    public List<PagoDTO> getByEstado(String estado) {
        return mapper.toPagosDTO(repo.findByEstado(estado));
    }
}