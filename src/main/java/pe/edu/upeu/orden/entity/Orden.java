package pe.edu.upeu.orden.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ordenes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_nombre", nullable = false, length = 120)
    private String clienteNombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;
}
