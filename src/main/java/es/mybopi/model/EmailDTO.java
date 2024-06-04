package es.mybopi.model;

import lombok.Data;

@Data
public class EmailDTO {

    private String destinatario;
    private String asunto;
    private String mensaje;

}
