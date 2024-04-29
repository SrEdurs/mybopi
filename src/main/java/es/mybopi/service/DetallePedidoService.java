package es.mybopi.service;

import org.springframework.stereotype.Service;

import es.mybopi.model.DetallePedido;

@Service
public interface DetallePedidoService {
    DetallePedido save(DetallePedido detalle);
}
