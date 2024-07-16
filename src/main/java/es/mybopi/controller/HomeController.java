package es.mybopi.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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
import es.mybopi.dto.StripeChargeDto;
import es.mybopi.model.EmailDTO;
import es.mybopi.model.Carrito;
import es.mybopi.model.Pedido;
import es.mybopi.model.Producto;
import es.mybopi.model.Usuario;
import es.mybopi.repository.ProductoRepository;
import es.mybopi.service.EmailService;
import es.mybopi.service.CarritoService;
import es.mybopi.service.PedidoService;
import es.mybopi.service.ProductoService;
import es.mybopi.service.StripeService;
import es.mybopi.service.UsuarioService;
import jakarta.mail.MessagingException;

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
    private StripeService stripeService;
    @Autowired
    private EmailService emailService;
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

        //Comprobar si hay datos de dirección
        if (usuario.getDireccion() != null && !usuario.getDireccion().trim().isEmpty() &&
            usuario.getNombre() != null && !usuario.getNombre().trim().isEmpty() &&
            usuario.getLocalidad() != null && !usuario.getLocalidad().trim().isEmpty() &&
            usuario.getTelefono() != null && !usuario.getTelefono().trim().isEmpty() &&
            usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty() &&
            usuario.getCP() != null && !usuario.getCP().trim().isEmpty()) {
            // Sumar 6,95 al total
            pedido.setTotal(pedido.getTotal() + 6.95);
        }
            model.addAttribute("usuario", usuario);
            model.addAttribute("pedido", pedido);
            return "usuarios/resumencompra";
        }
        else{
            return "redirect:/";
        }
    }




    @PostMapping("/guardarPedido")
    public String guardarPedido(@RequestParam("stripeToken") String stripeToken, EmailDTO email) throws MessagingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> userOptional = usuarioService.findByEmail(name);

        if (userOptional.isPresent()) {
            Usuario usuario = userOptional.get();
            List<Producto> productos = usuario.getCarrito().getProductos();
            List<Producto> productosCarrito = usuario.getCarrito().getProductos();
            double envio = 6.95;

            //Realizar el cargo en Stripe
            StripeChargeDto chargeRequest = new StripeChargeDto();
            chargeRequest.setStripeToken(stripeToken);
            chargeRequest.setAmount(String.valueOf(calcularTotal(productos) + envio)); // El total debería estar en centavos

            StripeChargeDto chargeResponse = stripeService.charge(chargeRequest);
            if (!chargeResponse.isSuccess()) {
                return "paymentError";
            }

            //Cadena de texto aleatoria
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder sb = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 8; i++) {
                int index = random.nextInt(chars.length());
                sb.append(chars.charAt(index));
            }
            String randomString = sb.toString();

            //Configurar y guardar el pedido
            Pedido pedido = new Pedido();
            pedido.setUsuario(usuario);
            pedido.setProductos(new ArrayList<>(productosCarrito));
            pedido.setTotal(calcularTotal(productosCarrito) + 6.95);
            pedido.setFecha(new Date());
            pedido.setNumero(pedidoService.generarNumPedido());
            pedido.setSeguimiento("Pendiente de envío");
            pedido.setEstado("En preparación");
            pedido.setCancelacion(false);
            pedido.setDevolucion(false);
            pedido.setToken(randomString);
            pedidoService.save(pedido);

            //Enviar correo con los datos
            email.setAsunto(pedido.getUsuario().getNombre() + " - Gracias por tu pedido en Mybopi");
            email.setDestinatario(usuario.getEmail());
            email.setTitulo("¡Muchas gracias por tu pedido!");
            email.setMensaje("Muchas gracias por hacer tu pedido en Mybopi, te lo prepararemos y enviaremos a la mayor brevedad posible.");
            email.setProductos(pedido.getProductos());
            email.setTotal(pedido.getTotal());
            emailService.sendMail(email);

            //Enviar correo a Mybopi
            email.setAsunto("Nuevo pedido en Mybopi - Pedido número: " + pedido.getNumero());
            email.setDestinatario("mybopii@gmail.com");
            email.setTitulo("Pedido " + pedido.getNumero());
            email.setMensaje("Se ha realizado un nuevo pedido de " + usuario.getNombre() + " con el siguiente número de pedido: " +
            pedido.getNumero() + " La dirección de envío es: " + pedido.getUsuario().getDireccion() + ", Localidad: " + pedido.getUsuario().getLocalidad() + ", CP: " + pedido.getUsuario().getCP() + ", Teléfono: " + pedido.getUsuario().getTelefono() + " Más información en los detalles del pedido de la app.");
            email.setProductos(pedido.getProductos());
            email.setTotal(pedido.getTotal());
            emailService.sendMail(email);

            //Actualizar el estado de los productos y limpiar el carrito del usuario
            for (Producto producto : productos) {
                producto.setVendido(true);
                producto.setPedido(pedido);
                productoService.save(producto);
            }
            usuario.getCarrito().getProductos().clear();
            usuario.getCarrito().setTotal(0);
            usuarioService.save(usuario);

            
            

            return "redirect:/gracias?num=" + randomString + "&id=" + pedido.getId();
        }

        return "redirect:/";
    }

    @GetMapping("/gracias")
    public String gracias(@RequestParam("num") String num, @RequestParam("id") Integer id, Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        Optional<Pedido> pedido = pedidoService.findById(id);
        if(pedido.isPresent()){
            if(num.equals(pedido.get().getToken())){
                List<Producto> productos = this.productoRepository.findTop4ByActivoOrderByFechaDesc(true);
                model.addAttribute("productosHome", productos);
                model.addAttribute("pedido", pedido.get());
                return "usuarios/graciaspedido";
            } else{
                return "redirect:/";
            }
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
        List<Pedido> pedidos = pedidoService.findByUsuarioIdOrderByFechaDesc(user.get().getId());
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

    @GetMapping("/devolver/{id}")
    public String devolverPedido(@PathVariable Integer id, Model model, @ModelAttribute("usuarioNav") Usuario usuario) {

        Optional<Pedido> pedido = pedidoService.findById(id);
        if(pedido.isPresent()){
            model.addAttribute("pedido", pedido.get());
            return "usuarios/devolucion";
        }
        return "usuarios/devolucion";
    }

    @GetMapping("pedidos/usuario/{id}")
    public String pedidosUsuario(@PathVariable Integer id, Model model, @ModelAttribute("usuarioNav") Usuario usuario) {

        List<Pedido> pedidos = pedidoService.findByUsuarioIdOrderByFechaDesc(id);
        model.addAttribute("pedidos", pedidos);
        return "usuarios/pedidos";
    }

    @PostMapping("/actualizarEstado/{id}")
    public String actualizarEstado(@ModelAttribute("id") Integer id, @RequestParam("estado") String estado, EmailDTO email) throws MessagingException {

       pedido = pedidoService.findById(id).get();
       pedido.setEstado(estado);
       pedidoService.save(pedido);

       if(pedido.getEstado().equals("Entregado")){
        email.setAsunto("Tu pedido de Mybopi se ha entregado");
        email.setDestinatario(pedido.getUsuario().getEmail());
        email.setTitulo("¡Tu pedido ha llegado!");
        email.setMensaje("Muchas gracias por tu compra. ¡Esperamos que lo disfrutes!");
        emailService.sendMail(email);
       }

       if(pedido.getEstado().equals("Cancelado")){
        pedido.setCancelacion(false);
        email.setAsunto("Tu pedido de Mybopi se ha cancelado - " + pedido.getNumero());
        email.setDestinatario(pedido.getUsuario().getEmail());
        email.setTitulo("Pedido numero " + pedido.getNumero());
        email.setMensaje("Tu pedido se ha cancelado correctamente y se ha realizado la devolución de la compra. Recibirás el importe completo en tu cuenta, esto podría demorarse entre 5 y 10 días dependiento de tu banco. ¡Esperamos que vuelvas a relaizar un pedido en Mybopi! Si tienes algún problema no dudes en ponerte en contacto con nosotros a través de nuestras redes sociales");
        emailService.sendMail(email);

        //Marcar todos los productos del pedido como no vendidos
        List<Producto> productos = pedido.getProductos();
        for (Producto producto : productos) {
            producto.setVendido(false);
            productoService.save(producto);
        }
       }

       return "redirect:/pedidos/" + id;        
    }

    @PostMapping("/actualizarSeguimiento/{id}")
    public String actualizarSeguimiento(@ModelAttribute("id") Integer id, @RequestParam("seguimiento") String seguimiento, EmailDTO email) throws MessagingException {

       pedido = pedidoService.findById(id).get();
       pedido.setSeguimiento(seguimiento);
       pedidoService.save(pedido);

       if(!pedido.getSeguimiento().equals("Pendiente de envío")){
        email.setAsunto("Tu pedido de Mybopi se ha enviado");
        email.setDestinatario(pedido.getUsuario().getEmail());
        email.setTitulo("Tu pedido de Mybopi se encuentra en camino!");
        email.setMensaje("Te informamos de que tu pedido ya se encuentra en camino. Lo recibirás dentro de 3 a 5 días laborables. ¡Ya falta poco para que puedas disfrutar!");
        email.setEnlace(seguimiento);
        emailService.sendMail(email);
       }

       return "redirect:/pedidos/" + id;        
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

    @GetMapping("/cancelar/{id}")
    public String cancelarPedido(@PathVariable Integer id, Model model, EmailDTO email) throws MessagingException {

        pedido = pedidoService.findById(id).get();
        pedido.setCancelacion(true);
        pedidoService.save(pedido);

        email.setAsunto("Cancelación del pedido " + pedido.getNumero());
        email.setDestinatario("mybopii@gmail.com");
        email.setTitulo("Cancelación del pedido " + pedido.getNumero());
        email.setMensaje("El usuario ha pedido la cancelación del pedido, al no haberse enviado, se puede hacer la devolución");
        email.setProductos(pedido.getProductos());
        email.setTotal(pedido.getTotal());
        emailService.sendMail(email);

        email.setAsunto("Confirmación de la cancelación del pedido " + pedido.getNumero());
        email.setDestinatario(pedido.getUsuario().getEmail());
        email.setTitulo("Cancelación del pedido " + pedido.getNumero());
        email.setMensaje("Te confirmamos que has solicitado la cancelación del pedido, procederemos a la devolución íntegra del importe a la mayor brevedad posible.");
        email.setProductos(pedido.getProductos());
        email.setTotal(pedido.getTotal());
        emailService.sendMail(email);


        
        return "redirect:/pedidos/" + id;
    }

        // Método para calcular el total de los productos en el carrito
        private int calcularTotal(List<Producto> productos) {
            int total = 0;
            for (Producto producto : productos) {
                total += producto.getPrecio(); // Suponiendo que el precio está en centavos
            }
            return total;
        }
    
}
