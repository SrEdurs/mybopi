package es.mybopi.service;

import org.springframework.stereotype.Service;

import es.mybopi.model.Usuario;

@Service
public interface UsuarioService {
    Usuario findById(Integer id);

}
