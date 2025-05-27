package com.clinica.pagos.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinica.pagos.domain.dto.PagoDTO;
import com.clinica.pagos.domain.service.PagoService;

@RestController
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private PagoService svc;

    @GetMapping
    public List<PagoDTO> getAll() {
        return svc.obtenerTodo();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoDTO> getById(@PathVariable Long id) {
        Optional<PagoDTO> p = svc.obtenerPorId(id);
        return p.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PagoDTO> create(@RequestBody PagoDTO dto) {
        return ResponseEntity.ok(svc.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagoDTO> update(
            @PathVariable Long id,
            @RequestBody PagoDTO dto) {
        return ResponseEntity.ok(svc.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (svc.eliminar(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/cita/{citaId}")
    public List<PagoDTO> getByCita(@PathVariable Long citaId) {
        return svc.obtenerPorCita(citaId);
    }
    
    @GetMapping("/estado/{estado}")
    public List<PagoDTO> getByEstado(@PathVariable String estado) {
        return svc.obtenerPorEstado(estado);
    }
}