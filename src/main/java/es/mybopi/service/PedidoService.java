package es.mybopi.service;

import java.util.List;
import org.springframework.stereotype.Service;
import es.mybopi.model.Pedido;

@Service
public interface PedidoService {
    List<Pedido> findAll();
    Pedido save(Pedido pedido);
    String generarNumPedido();
    List<Pedido> findByUsuario_Id(int id);
    

}
