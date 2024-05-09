package es.mybopi.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import es.mybopi.model.Carrito;

@Service
public interface CarritoService {

    public Carrito save(Carrito carrito);

    public Optional<Carrito> findByUsuarioId(Integer id);

}
