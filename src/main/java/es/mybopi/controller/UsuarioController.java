package es.mybopi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import es.mybopi.model.Usuario;
import es.mybopi.service.UsuarioService;
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

}
