package es.mybopi.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    public String save(@ModelAttribute Usuario user, @RequestParam("confirmPassword") String confirmPassword,
            EmailDTO email, RedirectAttributes redirectAttributes) throws MessagingException {
        if (!user.getPassword().equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordMismatch", true);
            return "redirect:/usuario/registro";
        }

        if (usuarioService.checkUserExists(user.getEmail())) {
            redirectAttributes.addFlashAttribute("emailInUse", true);
            return "redirect:/usuario/registro";
        }

        user.setPassword(encoder.encode(user.getPassword()));
        if ("mybopii@gmail.com".equals(user.getEmail())) {
            user.setRoles("ROLE_USER,ROLE_ADMIN");
        }
        usuarioService.save(user);
        email.setAsunto("¡Bienvenid@ a Mybopi!");
        email.setDestinatario(user.getEmail());
        email.setMensaje("Muchas gracias por registrarte, ahora tienes acceso a numerosos artículos pintados a mano!");
        emailService.sendMail(email);
        redirectAttributes.addFlashAttribute("accountCreated", true);
        return "redirect:/usuario/login";
    }

    @GetMapping("/password")
    public String password(@ModelAttribute("usuarioNav") Usuario usuario, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);
        Optional<Usuario> usu = usuarioService.findByEmail(name);
        if (user.isPresent()) {
            List<Producto> productos = this.productoRepository.findTop4ByActivoOrderByFechaDesc(true);
            model.addAttribute("productosHome", productos);
            model.addAttribute("usuarioSesion", usu.get());
            model.addAttribute("usuario", user.get());
            return "usuarios/password";
        } else {
            return "redirect:/";
        }

    }

    // Método para cambiar la contraseña
    @PostMapping("/password")
    public String savePassword(@ModelAttribute EmailDTO emailConfirma,
            @ModelAttribute("usuario") Usuario usuario,
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("repeatPassword") String repeatPassword,
            RedirectAttributes redirectAttributes) throws MessagingException {
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

            if (newPassword.equals(currentPassword) && newPassword.equals(repeatPassword)) {
                redirectAttributes.addFlashAttribute("error",
                        "La nueva contraseña no puede ser igual que la anterior.");
                return "redirect:/usuario/password";
            }

            currentUser.setPassword(encoder.encode(newPassword));
            usuarioService.save(currentUser);
            redirectAttributes.addFlashAttribute("success", "Contraseña cambiada con éxito.");

            // Mandamos un email al usuario
            emailConfirma.setAsunto("Cambio de contraseña Mybopi");
            emailConfirma.setDestinatario(user.get().getEmail());
            emailConfirma.setMensaje(
                    "Hola! Tu contraseña se ha cambiado correctamente. Si no has sido tu, por favor, ponte en contacto con nosotros.");
            emailService.sendMail(emailConfirma);
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
    public String recordarpass(@ModelAttribute Usuario user, EmailDTO email, @RequestParam("correo") String correo)
            throws MessagingException {

        // Cadena de texto al azar
        String randomText = "";
        for (int i = 0; i < 10; i++) {
            randomText += (char) (Math.random() * 26 + 'a');
        }

        // Si el email del usuario existe, mandar el mensaje
        Optional<Usuario> usuario = usuarioService.findByEmail(correo);
        if (usuario.isPresent()) {
            // Guardamos el token en la base de datos
            usuario.get().setToken(randomText);
            usuarioService.save(usuario.get());

            email.setAsunto("Cambio de contraseña");
            email.setDestinatario(usuario.get().getEmail());
            email.setMensaje(
                    "Hola! Hemos recibido una petición para cambiar la contraseña de tu cuenta. Si no es el caso, por favor, ignora este mensage");
            email.setEnlace2("http://localhost:8080/usuario/cambiapassword?token=" + randomText + "&email=" + correo);
            emailService.sendMail(email);

            return "redirect:/usuario/recordar?email=true";
        } else {
            return "redirect:/usuario/recordar?error=true";
        }
    }

    @GetMapping("/cambiapassword")
    public String cambiarPassword(@RequestParam("token") String token, @RequestParam("email") String email,
            Model model) {
        Optional<Usuario> user = usuarioService.findByEmail(email);
        if (user.isPresent() && user.get().getToken().equals(token)) {
            model.addAttribute("token", token);
            model.addAttribute("email", email);
            return "usuarios/cambiapassword";
        }
        return "redirect:/usuario/recordar?token=caducado";
    }

    @PostMapping("/cambiapassword")
    public String savePassword(@ModelAttribute EmailDTO emailConfirma, @RequestParam("token") String token,
            @RequestParam("email") String email,
            @RequestParam("password") String password, @RequestParam("confirmPassword") String confirmPassword)
            throws MessagingException {
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

            // Mandamos un email al usuario
            emailConfirma.setAsunto("Contraseña cambiada con éxito");
            emailConfirma.setDestinatario(email);
            emailConfirma.setMensaje(
                    "Hola! Tu contraseña se ha cambiado correctamente. Si no has sido tu, por favor, ponte en contacto con nosotros.");
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

        if (user.get().getActivo() == 0) {
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
    public String editarDatos(Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);
        Optional<Usuario> usu = usuarioService.findByEmail(name);
        if (user.get().getActivo() == 0) {
            return "redirect:/usuario/banned";
        }
        if (user.isPresent()) {
            List<Producto> productos = this.productoRepository.findTop4ByActivoOrderByFechaDesc(true);
            model.addAttribute("productosHome", productos);
            model.addAttribute("usuarioSesion", usu.get());
            model.addAttribute("usuario", user.get());
            return "usuarios/editarusuario";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/cuenta/editar")
    public String saveEditCuenta(RedirectAttributes redirectAttributes, @ModelAttribute("usuario") Usuario user) {
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
            usuarioService.save(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Tu cuenta se ha actualizado correctamente");
            return "redirect:/usuario/cuenta/editar";
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
            model.addAttribute("usuarioNav", usu.get());
            return "usuarios/cuenta";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> usu = usuarioService.findByEmail(name);
        Optional<Usuario> user = Optional.ofNullable(usuarioService.findById(id));

        if (user.isPresent()) {
            List<Producto> productos = this.productoRepository.findTop4ByActivoOrderByFechaDesc(true);
            model.addAttribute("productosHome", productos);
            model.addAttribute("usuarioSesion", usu.get());
            model.addAttribute("usuario", user.get());
            return "usuarios/editarusuario";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/edit/{id}")
    public String saveEdit(RedirectAttributes redirectAttributes, @ModelAttribute("id") Integer id,
            @ModelAttribute("usuario") Usuario user) {
        Optional<Usuario> usu = Optional.ofNullable(usuarioService.findById(id));
        if (usu.isPresent()) {
            Usuario usuario = usu.get();
            usuario.setNombre(user.getNombre());
            usuario.setApellidos(user.getApellidos());
            usuario.setDireccion(user.getDireccion());
            usuario.setCP(user.getCP());
            usuario.setLocalidad(user.getLocalidad());
            usuario.setTelefono(user.getTelefono());
            usuarioService.save(usuario);
            String mensaje = "La cuenta se ha actualizado correctamente";
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
            return "redirect:/usuario/edit/" + id;
        } else {
            return "redirect:/admin/usuarios";
        }
    }

    @GetMapping("/desactivar/{id}")
    public String desactivar(@ModelAttribute("id") Integer id) {
        Optional<Usuario> user = Optional.ofNullable(usuarioService.findById(id));
        if (user.isPresent()) {
            Usuario usuario = user.get();
            if (usuario.getActivo() == 1) {
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

    @GetMapping("/cuenta/email")
    public String editarEmail(Model model, @ModelAttribute("usuarioNav") Usuario usuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);
        Optional<Usuario> usu = usuarioService.findByEmail(name);

        if (user.isPresent()) {
            List<Producto> productos = this.productoRepository.findTop4ByActivoOrderByFechaDesc(true);
            model.addAttribute("productosHome", productos);
            model.addAttribute("usuarioSesion", usu.get());
            model.addAttribute("usuario", user.get());
            return "usuarios/cambiaremail";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/cuenta/email")
    public String saveEditEmail(@ModelAttribute EmailDTO emailConfirma, RedirectAttributes redirectAttributes,
            @ModelAttribute("usuario") Usuario user) throws MessagingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> usu = usuarioService.findByEmail(name);
        if (usu.isPresent()) {
            Usuario usuario = usu.get();
            if (user.getEmail().equals(usuario.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "El nuevo email no puede ser el actual");
                return "redirect:/usuario/cuenta/email";
            }
            // Comprobar si el email ya está en uso por otro usuario
            Optional<Usuario> user2 = usuarioService.findByEmail(user.getEmail());
            if (user2.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El email ya se encuentra en uso");
                return "redirect:/usuario/cuenta/email";
            }
            usuario.setEmail(user.getEmail());
            usuarioService.save(usuario);
            changeUsername(usuario.getEmail());
            // Mandamos un email al usuario
            emailConfirma.setAsunto("Cambio de email Mybopi");
            emailConfirma.setDestinatario(usuario.getEmail());
            emailConfirma.setMensaje(
                    "Hola! Tu email se ha cambiado correctamente. A partir de ahora usaremos este correo para las notificaciones.");
            emailService.sendMail(emailConfirma);
            redirectAttributes.addFlashAttribute("mensaje", "Tu email se ha actualizado correctamente");
            return "redirect:/usuario/cuenta/email";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/cuenta/borrar/{id}")
    public String marcarParaBorrado(@PathVariable("id") Integer id, @ModelAttribute EmailDTO emailConfirma)
            throws MessagingException {
        Optional<Usuario> user = Optional.ofNullable(usuarioService.findById(id));
        if (user.isPresent()) {
            Usuario usuario = user.get();
            usuario.setBorrando(true);
            usuario.setFechaBorrado(LocalDateTime.now());
            usuarioService.save(usuario);

            // Mandamos un email al usuario
            emailConfirma.setAsunto("Tu cuenta se ha marcado para el borrado");
            emailConfirma.setDestinatario(usuario.getEmail());
            emailConfirma.setMensaje(
                    "Hola! Tu cuenta se eliminará en 2 días desde tu solicitud. Borraremos toda tu información de nuestra base de datos. También puedes cancelar el borrado en cualquier momento desde el apartado de editar los datos de tu cuenta.");
            emailService.sendMail(emailConfirma);

            return "redirect:/usuario/cuenta/editar";
        }
        return "redirect:/error";
    }

    @GetMapping("/cuenta/cancelarborrado/{id}")
    public String cancelarBorrado(@PathVariable("id") Integer id, @ModelAttribute EmailDTO emailConfirma)
            throws MessagingException {
        Optional<Usuario> user = Optional.ofNullable(usuarioService.findById(id));
        if (user.isPresent()) {
            Usuario usuario = user.get();
            usuario.setBorrando(false);
            usuario.setFechaBorrado(null);
            usuarioService.save(usuario);

            // Mandamos un email al usuario
            emailConfirma.setAsunto("Se ha cancelado el borrado de tu cuenta");
            emailConfirma.setDestinatario(usuario.getEmail());
            emailConfirma.setMensaje("Hola! Hemos cancelado el borrado de tu cuenta conforme a tu solicitud.");
            emailService.sendMail(emailConfirma);

            return "redirect:/usuario/cuenta/editar";
        }
        return "redirect:/error";
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

    public static void changeUsername(String newUsername) {
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

        if (currentAuth != null) {
            // Crea un nuevo objeto de autenticación con el nuevo nombre de usuario
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    newUsername, currentAuth.getCredentials(), currentAuth.getAuthorities());

            // Actualiza el contexto de seguridad con el nuevo objeto de autenticación
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }
    }
}
