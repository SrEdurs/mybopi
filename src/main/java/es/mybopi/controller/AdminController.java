package es.mybopi.controller;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public String usuarios(Model model, @ModelAttribute("usuarioNav") Usuario usuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        // Validación de parámetros de paginación
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 5;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "nombre"));
        Page<Usuario> usuarios = this.usuarioService.findAllByOrderByIdAsc(pageable);

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usuarios.getTotalPages());
        model.addAttribute("totalItems", usuarios.getTotalElements());
        model.addAttribute("pageSize", size);
        return "admin/usuarios";
    }

    @GetMapping("/compras")
    public String compras(Model model, @ModelAttribute("usuarioNav") Usuario usuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Validación de parámetros de paginación
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        Page<Pedido> pedidos = this.pedidoService.findAllWithOrderByFechaDesc(pageable);
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pedidos.getTotalPages());
        model.addAttribute("totalItems", pedidos.getTotalElements());
        model.addAttribute("pageSize", size);
        return "admin/compras";
    }

    @ModelAttribute("usuarioNav")
    public Usuario usuarioNav(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);
        if (user.isPresent()) {
            model.addAttribute("usuarioNav", user.get());
            return user.get();
        } else {
            return new Usuario();
        }
    }
}
