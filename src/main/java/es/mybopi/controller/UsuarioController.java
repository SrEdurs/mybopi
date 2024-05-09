package es.mybopi.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import es.mybopi.model.Usuario;
import es.mybopi.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder encoder;

    @GetMapping("/registro")
    public String registro() {
        return "usuarios/registro";
    }
    
    @PostMapping("/save")
    public String save(@ModelAttribute Usuario user) {
        user.setPassword(encoder.encode(user.getPassword()));
        usuarioService.save(user);
        return "redirect:/usuario/login";
    }

    @GetMapping("/login")
    public String login() {
        return "usuarios/login";
    }

    @GetMapping("/cerrar")
    public String cerrar(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login";
    }

    @GetMapping("/cuenta")
    public String cuenta(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);
        if (user.isPresent()) {
            model.addAttribute("usuario", user.get());
            return "usuarios/cuenta";
        } else {
            return "redirect:/";
        } 
    }

    @GetMapping("/cuenta/editar")
    public String editarCuenta(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);
        if (user.isPresent()) {
            model.addAttribute("usuario", user.get());
            return "usuarios/editarUsuario";
        } else {
            return "redirect:/";
        } 
    }

    @GetMapping("/direccion/editar")
    public String editarDireccion(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);
        if (user.isPresent()) {
            model.addAttribute("usuario", user.get());
            return "usuarios/editarDireccion";
        } else {
            return "redirect:/";
        } 
    }

    @PostMapping("/cuenta/editar")
    public String saveEditCuenta(@ModelAttribute("usuario") Usuario user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> usu = usuarioService.findByEmail(name);
        if (usu.isPresent()) {
            Usuario usuario = usu.get();
            usuario.setUsername(user.getUsername());
            usuario.setEmail(user.getEmail());
            usuario.setNombre(user.getNombre());
            usuario.setApellidos(user.getApellidos());
            usuario.setTelefono(user.getTelefono());
            usuarioService.save(usuario);
            
            return "redirect:/usuario/cuenta";
        } else {
            return "redirect:/";
        }        
    }

    @PostMapping("/direccion/editar")
    public String saveEditDireccion(@ModelAttribute("usuario") Usuario user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> usu = usuarioService.findByEmail(name);
        if (usu.isPresent()) {
            Usuario usuario = usu.get();
            usuario.setNombre(user.getNombre());
            usuario.setApellidos(user.getApellidos());
            usuario.setDireccion(user.getDireccion());
            usuario.setCP(user.getCP());
            usuario.setLocalidad(user.getLocalidad());
            usuario.setTelefono(user.getTelefono());
            usuario.setEmail(user.getEmail());
            usuarioService.save(usuario);            
            return "redirect:/usuario/cuenta";
        } else {
            return "redirect:/";
        }        
    }



}
