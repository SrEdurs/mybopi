package es.mybopi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import es.mybopi.model.Pedido;
import es.mybopi.model.Producto;
import es.mybopi.model.Usuario;
import es.mybopi.service.PedidoService;
import es.mybopi.service.ProductoService;
import es.mybopi.service.UsuarioService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PedidoService pedidoService;

    @GetMapping("")
    public String home(Model model) {
        List<Producto> productos = this.productoService.findAll();
        model.addAttribute("productos", productos);
        return "admin/home";
    }

    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        List<Usuario> usuarios = this.usuarioService.findAll();
        model.addAttribute("usuarios", usuarios);
        return "admin/usuarios";
    }

    @GetMapping("/compras")
    public String compras(Model model) {
        List<Pedido> pedidos = this.pedidoService.findAllWithOrderByFechaDesc();
        model.addAttribute("pedidos", pedidos);
        return "admin/compras";
    }
    
}
