package es.mybopi.model;

import java.util.List;

import lombok.Data;

@Data
public class EmailDTO {

    private String destinatario;
    private String asunto;
    private String titulo;
    private String enlace;
    private String enlace2;
    private String mensaje;
    private List<Producto> productos;
    private double total;

    public EmailDTO() {
        // Constructor sin argumentos
    }

}
