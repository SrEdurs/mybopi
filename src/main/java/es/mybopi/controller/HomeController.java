package es.mybopi.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
import jakarta.servlet.http.HttpSession;

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
    public String home(Model model, HttpSession session) {
        List<Producto> productos = this.productoRepository.findTop4ByActivoOrderByFechaDesc(true);
        model.addAttribute("productosHome", productos);
        model.addAttribute("session", session.getAttribute("idusuario"));
        System.out.println("----------------------Session: " + session.getAttribute("idusuario"));
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
    public String addCarrito(@RequestParam("id") Integer id, Model model, HttpSession session) {

        Producto producto = new Producto();
        double sumaTotal = 0;

        Optional<Producto> optionalProducto = productoService.findById(id);
        if (optionalProducto.isPresent()) {

            //Comprobar si el producto ya se encuentra en el carrito
            for (Producto p : productosCarro) {
                if (p.getId() == id) {
                    return "redirect:/carrito";
                }
            }
            
                producto = optionalProducto.get();

                //Comprobar si el producto se ha vendido
                if (producto.isVendido()) {
                    producto.setPrecio(0);                   
                }

                //Añadir el producto al pedido
                productosCarro.add(producto);
                pedido.setProductos(productosCarro);
                
                //Calcular el total del pedido
                for (Producto p : productosCarro) {
                    sumaTotal += p.getPrecio();
                }
            
                Usuario usuario = usuarioService.findById(Integer.parseInt(session.getAttribute("idusuario").toString()));
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
        //Comprobar si algún producto se ha vendido
        for (Producto p : productosCarro) {
            if (p.isVendido()) {
                p.setPrecio(0);
            }
        }
        model.addAttribute("pedido", pedido);
        return "usuarios/carrito";
    }

    @GetMapping("/prepedido")
    public String preorder(Model model) {
       
        Iterator<Producto> iterator = productosCarro.iterator();

        while(iterator.hasNext()) {
            Producto p = iterator.next();
            if (p.isVendido()) {
                iterator.remove(); // Eliminar el producto usando el iterador
            }
        }

        return "redirect:/pedido";
    }

    @GetMapping("/pedido")
    public String order(Model model, HttpSession session) {

        /*for (Producto p : productosCarro) {
            if (p.isVendido()) {
                productosCarro.remove(p);
            }
        }*/

        Usuario usuario = usuarioService.findById(Integer.parseInt(session.getAttribute("idusuario").toString()));
        model.addAttribute("pedido", pedido);
        model.addAttribute("usuario", usuario);
        return "usuarios/resumencompra";
    }

    @GetMapping("/guardarPedido")
    public String guardarPedido(HttpSession session) {

        List<Producto> productos = new ArrayList<Producto>();

         for (int i = 0; i < productosCarro.size(); i++) {
            productos.add(productosCarro.get(i));
            productosCarro.get(i).setElPedido(pedido);
        }

        pedido.setProductos(productos);
        pedido.setTotal(pedido.getTotal());
        Date fechaPedido = new Date();
        pedido.setFecha(fechaPedido);
        pedido.setNumero(pedidoService.generarNumPedido());
        pedido.setUsuario(usuarioService.findById(Integer.parseInt(session.getAttribute("idusuario").toString())));
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

    @GetMapping("/pedidos")
    public String pedidos(Model model, HttpSession session) {
        List<Pedido> pedidos = pedidoService.findByUsuario_Id(Integer.parseInt(session.getAttribute("idusuario").toString()));
        model.addAttribute("pedidos", pedidos);
        return "usuarios/pedidos";
    }

    @GetMapping("/pedidos/{id}")
    public String pedidos(@PathVariable Integer id, Model model, HttpSession session) {
        Optional<Pedido> pedido = pedidoService.findById(id);
        if(pedido.isPresent()){
            model.addAttribute("pedido", pedido.get());
            return "usuarios/detallepedido";
        }
        return "usuarios/detallepedido";
    }
    
    
}
