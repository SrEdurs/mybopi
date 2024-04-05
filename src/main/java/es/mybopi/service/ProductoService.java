package es.mybopi.service;
import java.util.Optional;
import es.mybopi.model.Producto;

public interface ProductoService {

    public Producto save(Producto producto);

    public Optional<Producto> findById(Integer id);

    public void update(Producto producto);

    public void deleteById(Integer id);

}
