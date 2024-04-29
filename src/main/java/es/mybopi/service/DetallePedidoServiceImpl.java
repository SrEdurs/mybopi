package es.mybopi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.mybopi.model.DetallePedido;
import es.mybopi.repository.DetallePedidoRepository;

@Service
public class DetallePedidoServiceImpl implements DetallePedidoService {

    @Autowired
    private DetallePedidoRepository detalleRepository;
    @Override
    public DetallePedido save(DetallePedido detalle) {
        return detalleRepository.save(detalle);
    }

}
