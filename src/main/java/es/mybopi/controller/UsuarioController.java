package es.mybopi.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import es.mybopi.model.EmailDTO;
import es.mybopi.model.Producto;
import es.mybopi.model.Usuario;
import es.mybopi.repository.ProductoRepository;
import es.mybopi.service.EmailService;
import es.mybopi.service.UsuarioService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping("/registro")
    public String registro(@ModelAttribute("usuarioNav") Usuario usuario) {
        return "usuarios/registro";
    }
    
    @PostMapping("/save")
    public String save(@ModelAttribute Usuario user, EmailDTO email) throws MessagingException {
        user.setPassword(encoder.encode(user.getPassword()));
        usuarioService.save(user);
        email.setAsunto("¡Bienvenid@ a Mybopi!");
        email.setDestinatario(user.getEmail());
        email.setMensaje("Muchas gracias por registrarte, ahora tienes acceso a numerosos artículos pintados a mano!");
        emailService.sendMail(email);
        return "redirect:/usuario/login";
    }

    @GetMapping("/password")
    public String password(@ModelAttribute("usuarioNav") Usuario usuario, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);
        if (user.isPresent()) {
            model.addAttribute("usuario", user.get());
        }
        return "usuarios/password";
    }

    //Método para cambiar la contraseña
    @PostMapping("/password")
    public String savePassword(@ModelAttribute("usuario") Usuario usuario, 
                               @RequestParam("currentPassword") String currentPassword,
                               @RequestParam("newPassword") String newPassword,
                               @RequestParam("repeatPassword") String repeatPassword,
                               RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);

        if (user.isPresent()) {
            Usuario currentUser = user.get();
            if (!encoder.matches(currentPassword, currentUser.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta.");
                return "redirect:/usuario/password";
            }

            if (!newPassword.equals(repeatPassword)) {
                redirectAttributes.addFlashAttribute("error", "La nueva contraseña y la confirmación no coinciden.");
                return "redirect:/usuario/password";
            }

            currentUser.setPassword(encoder.encode(newPassword));
            usuarioService.save(currentUser);
            redirectAttributes.addFlashAttribute("success", "Contraseña cambiada con éxito.");
            return "redirect:/usuario/password?passwordChanged";
        }

        redirectAttributes.addFlashAttribute("error", "Error al cambiar la contraseña.");
        return "redirect:/usuario/password?passwordNOTChanged";
    }

    @GetMapping("/recordar")
    public String recordar() {
        return "usuarios/recordarpassword";
    }

    @PostMapping("/recordar")
    public String recordarpass(@ModelAttribute Usuario user, EmailDTO email, @RequestParam("correo") String correo) throws MessagingException{

         //Cadena de texto al azar
         String randomText = "";
         for (int i = 0; i < 10; i++) {
             randomText += (char) (Math.random() * 26 + 'a');
         }

        //Si el email del usuario existe, mandar el mensaje
        Optional<Usuario> usuario = usuarioService.findByEmail(correo);
        if (usuario.isPresent()) {
            //Guardamos el token en la base de datos
            usuario.get().setToken(randomText);
            usuarioService.save(usuario.get());

            email.setAsunto("Cambio de contraseña");
            email.setDestinatario(usuario.get().getEmail());
            email.setMensaje("Hola! Hemos recibido una petición para cambiar la contraseña de tu cuenta. Si no es el caso, por favor, ignora este mensage");
            email.setEnlace2("http://localhost:8080/usuario/cambiapassword?token=" + randomText + "&email=" + correo);
            emailService.sendMail(email);

            return "redirect:/usuario/recordar?email=true";
        } else {
            return "redirect:/usuario/recordar?error=true";
        } 
    }

    @GetMapping("/cambiapassword")
    public String cambiarPassword(@RequestParam("token") String token, @RequestParam("email") String email, Model model) {
        Optional<Usuario> user = usuarioService.findByEmail(email);
        if (user.isPresent() && user.get().getToken().equals(token)) {
            model.addAttribute("token", token);
            model.addAttribute("email", email);
            return "usuarios/cambiapassword";
        }
        return "redirect:/usuario/recordar?token=caducado";
    }


    @PostMapping("/cambiapassword")
    public String savePassword(@ModelAttribute EmailDTO emailConfirma, @RequestParam("token") String token, @RequestParam("email") String email,
                            @RequestParam("password") String password, @RequestParam("confirmPassword") String confirmPassword) throws MessagingException {
        // Verificar si las contraseñas coinciden
        if (!password.equals(confirmPassword)) {
            return "redirect:/usuario/cambiapassword?token=" + token + "&email=" + email + "&error=true";
        }

        // Usuario por email
        Optional<Usuario> user = usuarioService.findByEmail(email);
        if (user.isPresent()) {
            user.get().setPassword(encoder.encode(password));
            user.get().setToken("");
            usuarioService.save(user.get());

            //Mandamos un email al usuario
            emailConfirma.setAsunto("Cambio de contraseña");
            emailConfirma.setDestinatario(email);
            emailConfirma.setMensaje("Hola! Tu contraseña se ha cambiado correctamente. Si no has sido tu, por favor, ponte en contacto con nosotros.");
            emailService.sendMail(emailConfirma);
            
            return "redirect:/usuario/login?changed=true";
        }

        return "redirect:/usuario/recordar?token=invalid";
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
    public String cuenta(Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);
        if(user.get().getActivo() == 0) {
            return "redirect:/usuario/banned";
        }
        if (user.isPresent()) {
            List<Producto> productos = this.productoRepository.findTop4ByActivoOrderByFechaDesc(true);
            model.addAttribute("productosHome", productos);
            model.addAttribute("usuarioNav", usuario);
            model.addAttribute("usuarioSesion", user.get());
            model.addAttribute("usuario", user.get());
            return "usuarios/cuenta";
        } else {
            return "redirect:/";
        } 
    }

    @GetMapping("/cuenta/editar")
    public String editarDireccion(Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);
        if(user.get().getActivo() == 0) {
            return "redirect:/usuario/banned";
        }
        if (user.isPresent()) {
            model.addAttribute("usuario", user.get());
            return "usuarios/editarUsuario";
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

    @GetMapping("/perfil/{id}")
    public String perfil(Model model, @PathVariable("id") Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> usu = usuarioService.findByEmail(name);
        Optional<Usuario> user = Optional.ofNullable(usuarioService.findById(id));

        if (usu.isPresent()) {
            List<Producto> productos = this.productoRepository.findTop4ByActivoOrderByFechaDesc(true);
            model.addAttribute("productosHome", productos);
            model.addAttribute("usuarioSesion", usu.get());
            model.addAttribute("usuario", user.get());
            return "usuarios/cuenta";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @ModelAttribute("id") Integer id, @ModelAttribute("usuarioNav") Usuario usuario) {
        Optional<Usuario> user = Optional.ofNullable(usuarioService.findById(id));
        if (user.isPresent()) {
            model.addAttribute("usuario", user.get());
            return "usuarios/editarUsuario";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/edit/{id}")
    public String saveEdit(@ModelAttribute("id") Integer id, @ModelAttribute("usuario") Usuario user) {
        Optional<Usuario> usu = Optional.ofNullable(usuarioService.findById(id));
        if (usu.isPresent()) {
            Usuario usuario = usu.get();

            if(user.getNombre() == ""){
                usuario.setNombre(null);
            } else{
                usuario.setNombre(user.getNombre());
            }
            //Comprobar si user tiene campos en blanco
            if(user.getApellidos() == ""){
                usuario.setApellidos(null);
            } else{
                usuario.setApellidos(user.getApellidos());
            }
            if(user.getTelefono() == ""){
                usuario.setTelefono(null);
            } else{
                usuario.setTelefono(user.getTelefono());
            }

            usuario.setEmail(user.getEmail());
            usuarioService.save(usuario);          
            return "redirect:/usuario/perfil/" + id;
        } else {
            return "redirect:/admin/usuarios";
        }        
    }

    @GetMapping("/desactivar/{id}")
    public String desactivar(@ModelAttribute("id") Integer id) {
        Optional<Usuario> user = Optional.ofNullable(usuarioService.findById(id));
        if (user.isPresent()) {
            Usuario usuario = user.get();
            if(usuario.getActivo() == 1){
                usuario.setActivo(0);
            } else {
                usuario.setActivo(1);
            }
            usuarioService.save(usuario);
            return "redirect:/admin/usuarios";
        } else {
            return "redirect:/admin/usuarios";
        }
    }

    @GetMapping("/banned")
    public String banned() {
        return "usuarios/banned";
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
