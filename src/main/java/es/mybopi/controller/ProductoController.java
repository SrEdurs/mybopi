package es.mybopi.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import es.mybopi.model.Carrito;
import es.mybopi.model.Producto;
import es.mybopi.model.Usuario;
import es.mybopi.service.CarritoService;
import es.mybopi.service.ProductoService;
import es.mybopi.service.UploadFileService;
import es.mybopi.service.UsuarioService;

@Controller
@RequestMapping("/productos")
public class ProductoController {
    @Autowired
    private UploadFileService upload;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private CarritoService carritoService;

    @GetMapping("/inventario")
    public String detalles(Model model, @ModelAttribute("usuarioNav") Usuario usuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "true") Boolean estado) {

        // Validación de parámetros de paginación
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }

        Boolean activo = estado;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<Producto> inventario = productoService.findProductosActivosOrderByFechaDesc(activo, pageable);

        model.addAttribute("inventario", inventario);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", inventario.getTotalPages());
        model.addAttribute("totalItems", inventario.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("estado", activo);

        return "admin/inventario";
    }

    @GetMapping("/crear")
    public String crearProducto(@ModelAttribute("usuarioNav") Usuario usuario) {
        return "productos/crear";
    }

    @PostMapping("/guardar")
    public String guardar(Producto producto, @RequestParam("img1") MultipartFile file,
            @RequestParam("img2") MultipartFile file2, @RequestParam("img3") MultipartFile file3) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);

        if (user.isPresent()) {
            Usuario u = user.get();
            producto.setUsuario(u);

            // Fecha
            Date date = new Date();
            producto.setFecha(date);

            if (producto.getId() == null) { // Creando un producto
                String nombreImagen = upload.saveImage(file);
                producto.setPortada(nombreImagen);

                String nombreImagen2 = upload.saveImage(file2);
                producto.setImagen1(nombreImagen2);

                String nombreImagen3 = upload.saveImage(file3);
                producto.setImagen2(nombreImagen3);
            }

            productoService.save(producto);
            return "redirect:/productos/inventario";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        Producto producto = new Producto();
        Optional<Producto> optionalProducto = productoService.findById(id);
        producto = optionalProducto.get();
        model.addAttribute("producto", producto);
        return "productos/editar";
    }

    @PostMapping("/actualizar")
    public String actualizar(Producto producto, @RequestParam("img1") MultipartFile portada,
            @RequestParam("img2") MultipartFile imagen2, @RequestParam("img3") MultipartFile imagen3)
            throws IOException {
        Producto p = new Producto();
        p = productoService.findById(producto.getId()).get();
        Date date = new Date();
        producto.setFecha(date);

        if (portada.isEmpty()) {
            producto.setPortada(p.getPortada());
        } else { // Editando cambiando la imagen

            if (!p.getPortada().equals("default.jpg")) { // Si hay una portada por defecto
                upload.deleteImage(p.getPortada());
            }
            String nombreImagen = upload.saveImage(portada);
            producto.setPortada(nombreImagen);
        }

        // Imagen 1
        if (imagen2.isEmpty()) {
            producto.setImagen1(p.getImagen1());
        } else { // Editando cambiando la imagen
            if (!p.getImagen1().equals("default.jpg")) { // Si hay una imagen por defecto
                upload.deleteImage(p.getImagen1());
            }
            String nombreImagen = upload.saveImage(imagen2);
            producto.setImagen1(nombreImagen);
        }

        // Imagen 2
        if (imagen3.isEmpty()) {
            producto.setImagen2(p.getImagen2());
        } else { // Editando cambiando la imagen
            if (!p.getImagen2().equals("default.jpg")) { // Si hay una imagen por defecto
                upload.deleteImage(p.getImagen2());
            }
            String nombreImagen = upload.saveImage(imagen3);
            producto.setImagen2(nombreImagen);
        }

        // Comprobar si el producto está en algún carrito
        if (p.getCarritos() != null && !p.getCarritos().isEmpty()) {
            for (Carrito carrito : p.getCarritos()) {
                carrito.getProductos().forEach(prod -> {
                    if (prod.getId().equals(producto.getId())) {
                        prod.setPrecio(producto.getPrecio());
                    }
                });
                carrito.calcularTotal();
                carritoService.save(carrito); // Usar el método save del servicio
            }
        }

        producto.setUsuario(p.getUsuario());
        productoService.update(producto);
        return "redirect:/productos/inventario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Integer id) throws IOException {
        Producto p = new Producto();
        p = productoService.findById(id).get();
        // Si la portada es por defecto no la borramos
        if (!p.getPortada().equals("default.jpg")) {

            upload.deleteImage(p.getPortada());
        }
        // Si la imagen 1 es por defecto no la borramos
        if (!p.getImagen1().equals("default.jpg")) {

            upload.deleteImage(p.getImagen1());
        }
        // Si la imagen 2 es por defecto no la borramos
        if (!p.getImagen2().equals("default.jpg")) {

            upload.deleteImage(p.getImagen2());
        }
        // eliminar el producto de todos los carritos
        if (p.getCarritos() != null) {
            for (Carrito c : p.getCarritos()) {
                c.getProductos().remove(p);
                c.setTotal(c.getTotal() - p.getPrecio());
                carritoService.save(c);
            }
        }
        productoService.deleteById(id);
        return "redirect:/productos/inventario";
    }

    @ModelAttribute("usuarioNav")
    public Usuario usuarioNav(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);
        if (user.isPresent()) {
            model.addAttribute("usuarioNav", user.get());
            return user.get();
        } else {
            return new Usuario();
        }
    }
}
