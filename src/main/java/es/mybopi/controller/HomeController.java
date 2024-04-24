package es.mybopi.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.mybopi.model.DetallePedido;
import es.mybopi.model.Pedido;
import es.mybopi.model.Producto;
import es.mybopi.service.ProductoService;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private ProductoService productoService;

    private List<DetallePedido> detalles = new ArrayList<DetallePedido>();
    private Pedido pedido = new Pedido();

    @GetMapping("/")
    public String home(Model model) {
        List<Producto> productos = this.productoService.findAll();
        model.addAttribute("productosHome", productos);
        return "usuarios/index";
    }

    @GetMapping("producto/{id}")
    public String producto(@PathVariable Integer id, Model model) {
        Optional<Producto> optionalProducto = productoService.findById(id);
        if (optionalProducto.isPresent()) {
            model.addAttribute("producto", optionalProducto.get());
        }
        return "productos/producto";
    }

    @PostMapping("/carrito")
    public String addCarrito(@RequestParam("id") Integer id) {

        DetallePedido detallePedido = new DetallePedido();
        Producto producto = new Producto();
        double sumaTotal = 0;

        Optional<Producto> optionalProducto = productoService.findById(id);
        System.out.println("Producto añadido: " + optionalProducto.get().getNombre());
        System.out.println("-------------------------");
        System.out.println("Suma Total: " + sumaTotal);
        
        return "usuarios/carrito";
    }
    
}
