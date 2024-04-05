package es.mybopi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.mybopi.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    
}
