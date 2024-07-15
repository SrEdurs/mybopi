package es.mybopi.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import es.mybopi.model.Producto;

@Service
public interface ProductoService {

    public Producto save(Producto producto);

    public Optional<Producto> findById(Integer id);

    public void update(Producto producto);

    public void deleteById(Integer id);

    public List<Producto> findAll();

    public List<Producto> findTop4ByActivoOrderByFechaDesc(boolean activo);

    public Page<Producto> findProductosActivosOrderByFechaDesc(boolean activo, Pageable pageable);
}
