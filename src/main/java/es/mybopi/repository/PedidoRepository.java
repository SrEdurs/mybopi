package es.mybopi.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.mybopi.model.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByUsuario_Id(int id);
    Optional<Pedido> findById(int id);
    //Lista de todos los pedidos
    @Query("SELECT p FROM Pedido p ORDER BY p.fecha DESC")
    List<Pedido> findAllWithOrderByFechaDesc();

    //Pedidos de un usuario por ID de usuario ordenados por fecha descendente
    @Query("SELECT p FROM Pedido p WHERE p.usuario.id = ?1 ORDER BY p.fecha DESC")
    List<Pedido> findByUsuarioIdOrderByFechaDesc(int id);

}
