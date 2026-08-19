package pe.edu.upeu.orden.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenResponse {
    private Long id;
    private String clienteNombre;
    private String descripcion;
    private BigDecimal total;
    private String estado;
    private OffsetDateTime fechaCreacion;
}
