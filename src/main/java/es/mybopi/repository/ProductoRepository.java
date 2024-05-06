package es.mybopi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.mybopi.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    

    List<Producto> findByCategoriaAndActivo(int categoria, boolean activo);
    List<Producto> findByNombreContainingIgnoreCaseAndActivo(String nombre, boolean b);
    List<Producto> findTop4ByActivoOrderByFechaDesc(boolean activo);
    


    
}
