package es.mybopi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import es.mybopi.model.Producto;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final Logger LOGGER = LoggerFactory.getLogger(ProductoController.class);

    @GetMapping("")
    public String detalles(){
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
        return "redirect:/productos";
    }

}
