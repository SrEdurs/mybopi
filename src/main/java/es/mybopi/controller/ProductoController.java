package es.mybopi.controller;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


import es.mybopi.model.Producto;
import es.mybopi.model.Usuario;
import es.mybopi.service.ProductoService;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final Logger LOGGER = LoggerFactory.getLogger(ProductoController.class);
    
    @Autowired
    private ProductoService productoService;

    @GetMapping("")
    public String detalles(Model model){
        model.addAttribute("inventario", productoService.findAll());
        return "productos/detalles";
    }

    //Pantalla crear productos
    @GetMapping("/crear")
    public String crearProducto(){
        return "productos/crear";
    }

    //Guardar un nuevo producto
    @PostMapping("/guardar")
    public String guardar(Producto producto){
        LOGGER.info("Guardado {}", producto);

        Usuario u = new Usuario(1, "admin", "admin", "admin", "admin", "admin", null, 1, "admin", null, null);
        producto.setUsuario(u);
        productoService.save(producto);
        return "redirect:/productos";
    }

    //Pantalla editar producto
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model){
        Producto producto = new Producto();
        Optional<Producto> optionalProducto = productoService.findById(id);

        producto = optionalProducto.get();

        LOGGER.info("Editado {}", producto);

        model.addAttribute("producto", producto);

        return "productos/editar";
    }

    @PostMapping("/actualizar")
    public String actualizar(Producto producto){
        productoService.update(producto);
        return "redirect:/productos";

    }

}
