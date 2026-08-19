package pe.edu.upeu.orden.mapper;

import pe.edu.upeu.orden.dto.OrdenRequest;
import pe.edu.upeu.orden.dto.OrdenResponse;
import pe.edu.upeu.orden.entity.Orden;
import org.springframework.stereotype.Component;

@Component
public class OrdenMapper {

    public Orden toEntity(OrdenRequest request) {
        return Orden.builder()
                .clienteNombre(request.getClienteNombre())
                .descripcion(request.getDescripcion())
                .total(request.getTotal())
                .estado(request.getEstado())
                .build();
    }

    public OrdenResponse toResponse(Orden orden) {
        return OrdenResponse.builder()
                .id(orden.getId())
                .clienteNombre(orden.getClienteNombre())
                .descripcion(orden.getDescripcion())
                .total(orden.getTotal())
                .estado(orden.getEstado())
                .fechaCreacion(orden.getFechaCreacion())
                .build();
    }
}
