package es.mybopi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import es.mybopi.model.Pedido;

@Service
public interface PedidoService {
    List<Pedido> findAll();

    Pedido save(Pedido pedido);

    String generarNumPedido();

    List<Pedido> findByUsuario_Id(int id);

    Optional<Pedido> findById(int id);

    List<Pedido> findAllWithOrderByFechaDesc();

    Page<Pedido> findByUsuarioIdOrderByFechaDesc(int id, Pageable pageable);

    Page<Pedido> findAllWithOrderByFechaDesc(Pageable pageable);
}
