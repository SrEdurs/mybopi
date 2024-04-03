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

public class Usuario {
    
    private Integer id;
    private String nombre;
    private String apellidos;
    private String email;
    private String direccion;
    private String telefono;
    private boolean admin;
    private boolean activo;
    private String password;

    
}
