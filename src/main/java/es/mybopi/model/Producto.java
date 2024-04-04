package es.mybopi.model;


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

public class Producto {
    private Integer id;
    private String nombre;
    private double precio;
    private String imagen;
    private String imagen2;
    private String imagen3;
    private String video;
    private String categoria;
    private boolean activo;
}
