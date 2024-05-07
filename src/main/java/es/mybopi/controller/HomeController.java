package es.mybopi.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public String home(Model model) {
        List<Producto> productos = this.productoRepository.findTop4ByActivoOrderByFechaDesc(true);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);

        System.out.println("--------------------------------- " + name + " ---------------------------");

        if(user.isPresent()) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Usuario encontrado: " + user.get().getId());
        }

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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);


        if (optionalProducto.isPresent() && user.isPresent()) {
            Integer usuid = user.get().getId();

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
            
                Usuario usuario = usuarioService.findById(usuid);
                pedido.setUsuario(usuario);
                pedido.setFecha(new Date());
                pedido.setNumero(pedidoService.generarNumPedido());
                pedido.setTotal(sumaTotal);
                model.addAttribute("pedido", pedido);

                return "usuarios/carrito";
            
        } else{
            return "redirect:/carrito";
        }
        
        
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);

        if (user.isPresent()) {
            Usuario usuario = user.get();
            model.addAttribute("usuario", usuario);
            model.addAttribute("pedido", pedido);
            return "usuarios/resumencompra";
        } else{
            return "redirect:/";
        }
        
    }

    @GetMapping("/guardarPedido")
    public String guardarPedido() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);

        List<Producto> productos = new ArrayList<Producto>();

         for (int i = 0; i < productosCarro.size(); i++) {
            productos.add(productosCarro.get(i));
            productosCarro.get(i).setElPedido(pedido);
        }

        if (user.isPresent()) {
            pedido.setUsuario(user.get());
            pedido.setProductos(productos);
            pedido.setTotal(pedido.getTotal());
            Date fechaPedido = new Date();
            pedido.setFecha(fechaPedido);
            pedido.setNumero(pedidoService.generarNumPedido());
            pedidoService.save(pedido);
        } else{
            return "redirect:/";
        }

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
    public String pedidos(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);

        List<Pedido> pedidos = pedidoService.findByUsuario_Id(user.get().getId());
        model.addAttribute("pedidos", pedidos);
        return "usuarios/pedidos";
    }

    @GetMapping("/pedidos/{id}")
    public String pedidos(@PathVariable Integer id, Model model) {
        Optional<Pedido> pedido = pedidoService.findById(id);
        if(pedido.isPresent()){
            model.addAttribute("pedido", pedido.get());
            return "usuarios/detallepedido";
        }
        return "usuarios/detallepedido";
    }
    
    
}
