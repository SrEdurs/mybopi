package es.mybopi.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.mybopi.model.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    //Pedidos de un usuario
    List<Pedido> findByUsuario_Id(int id);

}
