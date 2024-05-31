package es.mybopi.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import es.mybopi.model.Pedido;
import es.mybopi.model.Usuario;
import es.mybopi.service.PedidoService;
import es.mybopi.service.UsuarioService;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private PedidoService pedidoService;

    @GetMapping("/usuarios")
    public String usuarios(Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        List<Usuario> usuarios = this.usuarioService.findAll();
        model.addAttribute("usuarios", usuarios);
        return "admin/usuarios";
    }

    @GetMapping("/compras")
    public String compras(Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        List<Pedido> pedidos = this.pedidoService.findAllWithOrderByFechaDesc();
        model.addAttribute("pedidos", pedidos);
        return "admin/compras";
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
