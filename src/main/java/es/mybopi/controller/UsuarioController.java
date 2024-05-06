package es.mybopi.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import es.mybopi.model.Usuario;
import es.mybopi.service.UsuarioService;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @GetMapping("/registro")
    public String registro() {
        return "usuarios/registro";
    }
    
    @PostMapping("/save")
    public String save(@ModelAttribute Usuario user) {
        //usuario.setAdmin("NO");

        if(usuarioService.checkUserExists(user.getEmail())){
            return "redirect:/registro";
        } else{
            Integer id = usuarioService.saveUser(user);
            String message = "User '"+id+"' saved successfully !";
            return String.format("redirect:/login");
        } 
        
        //Confirmar si el usuario existe, si no, guardarlo
        /*Optional<Usuario> usuarioExistente = usuarioService.findByEmail(usuario.getEmail());
        if (usuarioExistente.isPresent()) {
            return "redirect:/registro";
        } else{
            usuario.setAdmin("NO");
            System.out.println("------------------------ USUARIO " + usuario.getNombre());
            usuario.setPassword(encoder.encode(usuario.getPassword()));
            usuario.setActivo(1);
            usuario.setEmail(usuario.getEmail());
            usuario.setNombre(usuario.getNombre());
            usuarioService.save(usuario);
            System.out.println("----------------------- Guardado " + usuario);
        } 
        System.out.println("No se ha guardado nada");
        
        
        return "redirect:/"; */
    }

    @GetMapping("/login")
    public String login() {
        return "usuarios/login";
    }

   /*  @PostMapping("/acceder")
    public String acceder(Usuario usuario, HttpSession session) {
        System.out.println("----------------------- Accediendo -------------------------- " + usuario.getEmail());
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
    } */

    @GetMapping("/cerrar")
    public String cerrar(HttpSession session) {
        System.out.println("----------------------- Cerrando sesion");
        session.removeAttribute("idusuario");
        return "redirect:/";
    }

}
