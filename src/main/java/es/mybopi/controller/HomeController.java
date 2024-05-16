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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import es.mybopi.model.Carrito;
import es.mybopi.model.Pedido;
import es.mybopi.model.Producto;
import es.mybopi.model.Usuario;
import es.mybopi.repository.ProductoRepository;
import es.mybopi.service.CarritoService;
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
    private CarritoService carritoService;

    @Autowired
    private PedidoService pedidoService;

    private Pedido pedido = new Pedido();

    @GetMapping("/")
    public String home(Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        List<Producto> productos = this.productoRepository.findTop4ByActivoOrderByFechaDesc(true);
        model.addAttribute("productosHome", productos);
        return "usuarios/index";
    }

    @GetMapping("/totebags")
    public String totebags(Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        List<Producto> totebags = this.productoRepository.findByCategoriaAndActivoOrderByFechaDesc(1, true);
        model.addAttribute("inventario", totebags);
        return "usuarios/totebags";
    }

    @GetMapping("/mochilas")
    public String mochilas(Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        List<Producto> mochilas = this.productoRepository.findByCategoriaAndActivoOrderByFechaDesc(2, true);
        model.addAttribute("inventario", mochilas);
        return "usuarios/mochilas";
    }

    @GetMapping("producto/{id}")
    public String producto(@PathVariable Integer id, Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        Optional<Producto> optionalProducto = productoService.findById(id);
        if (optionalProducto.isPresent()) {
            model.addAttribute("producto", optionalProducto.get());
        }
        return "productos/producto";
    }

    @PostMapping("/carrito")
    public String addCarrito(@RequestParam("id") Integer id, Model model) {
    
        Optional<Producto> optionalProducto = productoService.findById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);
        if(user.get().getActivo() == 0) {
            return "redirect:/usuario/banned";
        }
    
        if (optionalProducto.isPresent() && user.isPresent()) {
            Usuario usuario = user.get();
            Carrito carrito = new Carrito();
    
            if (usuario.getCarrito() == null) {
                usuario.setCarrito(carrito);
                carrito.setUsuario(usuario);
                carritoService.save(carrito);
                usuarioService.save(usuario);
            }

            carrito = usuario.getCarrito();

            if(carrito.getProductos() == null) {
                carrito.setProductos(new ArrayList<Producto>());
            }            
    
            // Verificar si el producto ya está en el carrito
            boolean productoYaExiste = carrito.getProductos().stream().anyMatch(p -> p.getId().equals(id));
            if (!productoYaExiste) {
                carrito.getProductos().add(optionalProducto.get());
                optionalProducto.get().getCarritos().add(carrito);
            }
        
            // Calcular el total del carrito
            double sumaTotal = carrito.getProductos().stream().mapToDouble(Producto::getPrecio).sum();
            carrito.setTotal(sumaTotal);
            carrito.setUsuario(usuario);
            carritoService.save(carrito);
            model.addAttribute("carrito", carrito);
            return "redirect:/carrito";
        } else {  
            return "redirect:/carrito";
        }
    }

    //Quitar un producto del carrito
   @GetMapping("/quitar/{id}")
   public String quitarProducto(@PathVariable Integer id, Model model) {
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       String name = authentication.getName();
       Optional<Usuario> user = usuarioService.findByEmail(name);
       if (user.isPresent()) {
           Carrito carrito = user.get().getCarrito();
           Optional<Producto> productoToRemove = carrito.getProductos().stream()
                                                   .filter(p -> p.getId().equals(id))
                                                   .findFirst();
           if (productoToRemove.isPresent()) {
               Producto producto = productoToRemove.get();
               carrito.getProductos().remove(producto);
               carrito.setTotal(carrito.getTotal() - producto.getPrecio());
               producto.getCarritos().remove(carrito);

               carritoService.save(carrito);
           }
       }
       return "redirect:/carrito";
   }


    //Carrito
    @GetMapping("/carrito")
    public String carrito(Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        //Carrito del usuario
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);
        if(user.get().getActivo() == 0) {
            return "redirect:/usuario/banned";
        }

        //Comprobar si el usuario tiene un carrito
        if (user.isPresent() && user.get().getCarrito() == null) {
            Carrito carrito = new Carrito();
            carrito.setUsuario(user.get());
            carrito.setProductos(new ArrayList<Producto>());
            carritoService.save(carrito);
            user.get().setCarrito(carrito);
            usuarioService.save(user.get());
        }
        if (user.isPresent()) {
            //Comprobar si un producto se ha vendido
            for (Producto p : user.get().getCarrito().getProductos()) {
                if (p.isVendido()) {
                    user.get().getCarrito().getProductos().remove(p);
                    user.get().getCarrito().setTotal(user.get().getCarrito().getTotal() - p.getPrecio());

                    //guardar el carrito
                    carritoService.save(user.get().getCarrito());
                    break;
                }
            }
            model.addAttribute("carrito", user.get().getCarrito());
        }
        return "usuarios/carrito";
    }

    
    @GetMapping("/pedido")
    public String order(Model model, @ModelAttribute("usuarioNav") Usuario usuarion) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);

        if (user.isPresent()) {
            Usuario usuario = user.get();
            pedido.setUsuario(usuario);
            //Comprobar si algún producto ha cambiado de precio
            for (Producto p : usuario.getCarrito().getProductos()) {
                if(p.getPrecio() != productoService.findById(p.getId()).get().getPrecio()) {
                    p.setPrecio(productoService.findById(p.getId()).get().getPrecio());
                }
            }

            pedido.setTotal(usuario.getCarrito().getTotal());
            pedido.setProductos(usuario.getCarrito().getProductos());

            Iterator<Producto> iterator = pedido.getProductos().iterator();

        while(iterator.hasNext()) {
            Producto p = iterator.next();
            if (p.isVendido()) {
                iterator.remove();
            }
        }
            model.addAttribute("usuario", usuario);
            model.addAttribute("pedido", pedido);
            return "usuarios/resumencompra";
        }
        else{
            return "redirect:/";
        }
    }

    @GetMapping("/guardarPedido")
    public String guardarPedido() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);

        if(user.isPresent()) {

            Usuario usuario = user.get();
            List<Producto> productos = new ArrayList<Producto>();

            for (int i = 0; i < usuario.getCarrito().getProductos().size(); i++) {
                productos.add(usuario.getCarrito().getProductos().get(i));
                usuario.getCarrito().getProductos().get(i).setElPedido(pedido);
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

            for (Producto p : productos) {
                p.setVendido(true);
                productoService.save(p);
            }       

            pedido = new Pedido();
            usuario.getCarrito().getProductos().clear();
            return "redirect:/";
        }

        return "redirect:/";
    }

    @GetMapping("/buscar")
    public String buscarProducto(@RequestParam("nombre") String nombre, Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        final List<Producto> productos = this.productoRepository.findByNombreContainingIgnoreCaseAndActivo(nombre, true);
        //Recorremos el list por consola
        for (Producto p : productos) {
            System.out.println(p.getNombre());
        }
        model.addAttribute("producto", productos);
        return "usuarios/busqueda";
    }

    @GetMapping("/pedidos")
    public String pedidos(Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);

        List<Pedido> pedidos = pedidoService.findByUsuario_Id(user.get().getId());
        model.addAttribute("pedidos", pedidos);
        return "usuarios/pedidos";
    }

    @GetMapping("/pedidos/{id}")
    public String pedidos(@PathVariable Integer id, Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        Optional<Pedido> pedido = pedidoService.findById(id);
        if(pedido.isPresent()){
            model.addAttribute("pedido", pedido.get());
            return "usuarios/detallepedido";
        }
        return "usuarios/detallepedido";
    }

    @GetMapping("/pedidos/usuario/{id}")
    public String pedidosUsuario(@PathVariable Integer id, Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        List<Pedido> pedidos = pedidoService.findByUsuario_Id(id);
        model.addAttribute("pedidos", pedidos);
        return "usuarios/pedidos";
    }
    

    @ModelAttribute("usuarioNav")
    public Usuario usuarioNav(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);
        if(user.isPresent()) {
            model.addAttribute("usuarioNav",user.get());     
            return user.get();
        } else{
            return new Usuario();
        }
    }
    
}
