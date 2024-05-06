package es.mybopi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import es.mybopi.model.Usuario;

@Service
public interface UsuarioService {
    Usuario findById(Integer id);
    Usuario save(Usuario usuario);
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findAll();
    public boolean checkUserExists(String email);
    public Integer saveUser(Usuario usuario);
}
