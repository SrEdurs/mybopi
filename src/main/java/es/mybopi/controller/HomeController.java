package es.mybopi.controller;

import java.util.ArrayList;
import java.util.Date;
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
import es.mybopi.model.Usuario;
import es.mybopi.repository.ProductoRepository;
import es.mybopi.service.DetallePedidoService;
import es.mybopi.service.PedidoService;
import es.mybopi.service.ProductoService;
import es.mybopi.service.UsuarioService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private DetallePedidoService detalleService;

    private List<DetallePedido> detalles = new ArrayList<DetallePedido>();
    private Pedido pedido = new Pedido();

    @GetMapping("/")
    public String home(Model model) {
        List<Producto> productos = this.productoService.findAll();
        model.addAttribute("productosHome", productos);
        return "usuarios/index";
    }

    @GetMapping("/totebags")
    public String totebags(Model model) {
        List<Producto> totebags = this.productoRepository.findByCategoriaAndActivo(1, true);
        model.addAttribute("inventario", totebags);
        return "usuarios/totebags";
    }

    @GetMapping("/mochilas")
    public String mochilas(Model model) {
        List<Producto> mochilas = this.productoRepository.findByCategoriaAndActivo(2, true);
        model.addAttribute("inventario", mochilas);
        return "usuarios/mochilas";
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

            //Validar que el producto no se añado 2 veces
            boolean encontrado = false;
            for (DetallePedido dp : detalles) {
                if (dp.getProducto().getId() == producto.getId()) {
                    encontrado = true;
                    break;
                }
            }
            if (encontrado) {
                System.out.println("El producto ya se encuentra en el carrito");
                return "redirect:/carrito";
            }

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

    @GetMapping("/pedido")
    public String order(Model model) {
        Usuario usuario = usuarioService.findById(1);
        model.addAttribute("detalles", detalles);
        model.addAttribute("pedido", pedido);
        model.addAttribute("usuario", usuario);
        return "usuarios/resumencompra";
    }

    @GetMapping("/guardarPedido")
    public String guardarPedido() {
        Date fechaPedido = new Date();
        pedido.setFecha(fechaPedido);
        pedido.setNumero(pedidoService.generarNumPedido());
        pedido.setUsuario(usuarioService.findById(1));
        pedidoService.save(pedido);

        for (DetallePedido dp : detalles) {
            dp.setPedido(pedido);
            detalleService.save(dp);
        }

        pedido = new Pedido();
        detalles.clear();

        return "redirect:/";
    }

    @GetMapping("/buscar")
    public String buscarProducto(@RequestParam("nombre") String nombre, Model model) {
        final List<Producto> productos = this.productoRepository.findByNombreContainingIgnoreCaseAndActivo(nombre, true);
        //Recorremos el list por consola
        for (Producto p : productos) {
            System.out.println(p.getNombre());
        }
        model.addAttribute("producto", productos);
        return "usuarios/busqueda";
    }
    
    
}
