package es.mybopi.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import es.mybopi.model.Usuario;
import es.mybopi.service.UsuarioService;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/registro")
    public String registro() {
        return "usuarios/registro";
    }
    
    @PostMapping("/save")
    public String save(Usuario usuario) {
        usuario.setAdmin("NO");
        System.out.println("----------------------- Guardado " + usuario);
        usuarioService.save(usuario);
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "usuarios/login";
    }

    @PostMapping("/acceder")
    public String acceder(Usuario usuario, HttpSession session) {
        System.out.println("----------------------- Accediendo " + usuario);

        Optional<Usuario> optionalUsuario = usuarioService.findByEmail(usuario.getEmail());

        if (optionalUsuario.isPresent() && optionalUsuario.get().getPassword().equals(usuario.getPassword())) {

            System.out.println("Usuario accediendo: " + optionalUsuario.get().getNombre());

            session.setAttribute("idusuario", optionalUsuario.get().getId());
            session.setAttribute("nombreusuario", optionalUsuario.get().getNombre());
            session.setAttribute("admin", optionalUsuario.get().getAdmin());


        
            if (optionalUsuario.get().getAdmin().equals("SI")) {
                return "redirect:/admin";
            } else {
                return "redirect:/";
            }
        }

        System.out.println("----------------------- Accediendo fallido");
        return "redirect:/";
    }

}
