package es.mybopi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.mybopi.model.Usuario;
import es.mybopi.repository.UsuarioRepository;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Override
    public Usuario findById(Integer id) {
        return usuarioRepository.findById(id).get();
    }

}
