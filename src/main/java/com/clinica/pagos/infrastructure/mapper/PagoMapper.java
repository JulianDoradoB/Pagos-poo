package com.clinica.pagos.infrastructure.mapper;

import java.util.List;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.clinica.pagos.domain.dto.PagoDTO;
import com.clinica.pagos.infrastructure.entity.Pago;

@Mapper(componentModel = "spring")
public interface PagoMapper {

    @Mapping(target = "nombrePaciente", ignore = true)
    @Mapping(target = "nombreMedico", ignore = true)
    PagoDTO toPagoDTO(Pago pago);

    List<PagoDTO> toPagosDTO(List<Pago> pagos);

    @InheritInverseConfiguration
    Pago toPago(PagoDTO dto);

    List<Pago> toPagos(List<PagoDTO> dtos);
}