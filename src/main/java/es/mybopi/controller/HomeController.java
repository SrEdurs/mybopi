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
    public String addCarrito(@RequestParam("id") Integer id, Model model) {

        DetallePedido detallePedido = new DetallePedido();
        Producto producto = new Producto();
        double sumaTotal = 0;

        Optional<Producto> optionalProducto = productoService.findById(id);
        System.out.println("Producto añadido: " + optionalProducto.get().getNombre());
        System.out.println("-------------------------");

        if (optionalProducto.isPresent()) {
            producto = optionalProducto.get();
            detallePedido.setNombre(producto.getNombre());
            detallePedido.setPrecio(producto.getPrecio());
            detallePedido.setProducto(producto);

            detalles.add(detallePedido);

            //calcular sumaTotal
            for (DetallePedido dp : detalles) {
                sumaTotal += dp.getPrecio();
            }


            System.out.println("--------------"+sumaTotal);
            pedido.setTotal(sumaTotal);

            System.out.println("--------------"+pedido.getTotal());
            model.addAttribute("detalles", detalles);
            model.addAttribute("pedido", pedido);
            
        }
        
        return "usuarios/carrito";
    }

    //Quitar un producto del carrito
    @GetMapping("/quitar/{id}")
    public String quitarProducto(@PathVariable Integer id, Model model) {

        //Lista nueva de productos
        List<DetallePedido> nuevaLista = new ArrayList<DetallePedido>();
        for (DetallePedido dp : detalles) {
            if (dp.getProducto().getId() != id) {
                nuevaLista.add(dp);
            }
        }
        detalles = nuevaLista;

        //calcular sumaTotal
        double sumaTotal = 0;
        for (DetallePedido dp : detalles) {
            sumaTotal += dp.getPrecio();
        }
        pedido.setTotal(sumaTotal);
        model.addAttribute("detalles", detalles);
        model.addAttribute("pedido", pedido);

        return "redirect:/carrito";
    }


    //Carrito
    @GetMapping("/carrito")
    public String carrito(Model model) {
        model.addAttribute("detalles", detalles);
        model.addAttribute("pedido", pedido);
        
        return "usuarios/carrito";
    }
    
}
