package es.mybopi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.mybopi.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);

    @SuppressWarnings("null")
    List<Usuario> findAll();

    List<Usuario> findAllByBorrandoTrue();
}
