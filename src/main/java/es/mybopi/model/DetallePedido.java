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


public class DetallePedido {
    private Integer id;
    private String nombre;
    private double cantidad;
    private double precio;
    private double total;
    
}
