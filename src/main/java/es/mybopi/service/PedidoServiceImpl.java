package es.mybopi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import es.mybopi.model.Pedido;
import es.mybopi.repository.PedidoRepository;

@Service
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Override
    public Pedido save(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    @Override
    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    public String generarNumPedido() {
        int numero = 0;
        String numconcact = "";

        List<Pedido> pedidos = pedidoRepository.findAll();
        List<Integer> numeros = new ArrayList<Integer>();

        pedidos.stream().forEach(p -> numeros.add(Integer.parseInt(p.getNumero())));
        if (pedidos.isEmpty()) {
            numero = 1;
        } else {
            numero = numeros.stream().max(Integer::compare).get();
            numero++;
        }

        if (numero < 10) {
            numconcact = "00000" + String.valueOf(numero);
        } else if (numero < 100) {
            numconcact = "0000" + String.valueOf(numero);
        } else if (numero < 1000) {
            numconcact = "000" + String.valueOf(numero);
        } else if (numero < 10000) {
            numconcact = "00" + String.valueOf(numero);
        } else if (numero < 100000) {
            numconcact = "0" + String.valueOf(numero);
        }

        return numconcact;
    }

    @Override
    public List<Pedido> findByUsuario_Id(int id) {
        return pedidoRepository.findByUsuario_Id(id);
    }

    @Override
    public Optional<Pedido> findById(int id) {
        return pedidoRepository.findById(id);
    }

    @Override
    public List<Pedido> findAllWithOrderByFechaDesc() {
        return pedidoRepository.findAllWithOrderByFechaDesc();
    }


    @Override
    public Page<Pedido> findAllWithOrderByFechaDesc(Pageable pageable) {
        return pedidoRepository.findAllWithOrderByFechaDesc(pageable);
    }

    @Override
    public Page<Pedido> findByUsuarioIdOrderByFechaDesc(int id, Pageable pageable) {
        return pedidoRepository.findByUsuarioIdOrderByFechaDesc(id, pageable);
    }
}
