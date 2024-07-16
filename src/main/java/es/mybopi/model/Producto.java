package es.mybopi.model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private double precio;
    private String portada;
    private String imagen1;
    private String imagen2;
    private String descripcion;
    private int categoria;
    private boolean activo = true;
    private boolean vendido = false;
    private Date fecha;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "elPedido_id", nullable = true)
    private Pedido pedido;

    @ManyToMany(mappedBy = "productos")
    private List<Carrito> carritos;
}
