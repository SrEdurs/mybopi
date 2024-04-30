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
import es.mybopi.model.Pedido;
import es.mybopi.model.Producto;
import es.mybopi.model.Usuario;
import es.mybopi.repository.ProductoRepository;
import es.mybopi.service.PedidoService;
import es.mybopi.service.ProductoService;
import es.mybopi.service.UsuarioService;
import org.springframework.web.bind.annotation.PostMapping;



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
    private Pedido pedido = new Pedido();
    private List<Producto> productosCarro = new ArrayList<Producto>();

    @GetMapping("/")
    public String home(Model model) {
        List<Producto> productos = this.productoRepository.findTop4ByActivoOrderByFechaDesc(true);
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

        Producto producto = new Producto();
        double sumaTotal = 0;

        Optional<Producto> optionalProducto = productoService.findById(id);
        if (optionalProducto.isPresent()) {
            producto = optionalProducto.get();

            //Añadir el producto al pedido
            productosCarro.add(producto);
            pedido.setProductos(productosCarro);
            
            //Calcular el total del pedido
            for (Producto p : productosCarro) {
                sumaTotal += p.getPrecio();
            }
        
            Usuario usuario = usuarioService.findById(1);
            pedido.setUsuario(usuario);
            pedido.setFecha(new Date());
            pedido.setNumero(pedidoService.generarNumPedido());
            pedido.setTotal(sumaTotal);
            model.addAttribute("pedido", pedido);
            
        }
        
        return "usuarios/carrito";
    }

    //Quitar un producto del carrito
    @GetMapping("/quitar/{id}")
    public String quitarProducto(@PathVariable Integer id, Model model) {

        //Quitar producto del pedido
        List<Producto> nuevaListaPedido = new ArrayList<Producto>();
        for (Producto p : productosCarro) {
            if (p.getId() != id) {
                nuevaListaPedido.add(p);
            }
        }
        productosCarro = nuevaListaPedido;
        pedido.setProductos(productosCarro);

        //Calcular el total del pedido
        double sumaTotal = 0;
        for (Producto p : productosCarro) {
            sumaTotal += p.getPrecio();
        }
        pedido.setTotal(sumaTotal);
        model.addAttribute("pedido", pedido);

        return "redirect:/carrito";
    }


    //Carrito
    @GetMapping("/carrito")
    public String carrito(Model model) {
        model.addAttribute("pedido", pedido);
        return "usuarios/carrito";
    }

    @GetMapping("/pedido")
    public String order(Model model) {
        Usuario usuario = usuarioService.findById(1);
        model.addAttribute("pedido", pedido);
        model.addAttribute("usuario", usuario);
        return "usuarios/resumencompra";
    }

    @GetMapping("/guardarPedido")
    public String guardarPedido() {

        
        System.out.println("-------------------00000000000--------------");
        List<Producto> productos = new ArrayList<Producto>();

        System.out.println("-------------------11111111--------------");
         for (int i = 0; i < productosCarro.size(); i++) {
            System.out.println("-------------------111111555555555--------------");
            productos.add(productosCarro.get(i));
            productosCarro.get(i).setElPedido(pedido);
        }

        System.out.println("-------------------2222222222--------------");

        pedido.setProductos(productos);
        System.out.println("-------------------33333333333--------------");
        pedido.setTotal(pedido.getTotal());
        System.out.println("-------------------44444444444444--------------");
        Date fechaPedido = new Date();
        System.out.println("-------------------5555555555555--------------");
        pedido.setFecha(fechaPedido);
        System.out.println("-------------------666666666666666--------------");
        pedido.setNumero(pedidoService.generarNumPedido());
        System.out.println("-------------------777777777777--------------");
        pedido.setUsuario(usuarioService.findById(1));
        System.out.println("-------------------888888888888888--------------");
        pedidoService.save(pedido);

        //Guardar los productos del list
        for (Producto p : productos) {
            p.setVendido(true);
            productoService.save(p);
        }       

        pedido = new Pedido();
        productosCarro.clear();
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
