package es.mybopi.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import es.mybopi.model.EmailDTO;
import es.mybopi.model.Pedido;
import es.mybopi.model.Usuario;
import es.mybopi.repository.CarritoRepository;
import es.mybopi.repository.PedidoRepository;
import es.mybopi.repository.UsuarioRepository;
import jakarta.mail.MessagingException;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private CarritoRepository carritoRepository;
    @Autowired
    private PedidoRepository pedidoRepository;

    @Override
    public Usuario findById(Integer id) {
        return usuarioRepository.findById(id).get();
    }
    @Override
    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    @Override
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }
    @Override
    public boolean checkUserExists(String email) {
        return findByEmail(email).isPresent();
    }
    @Override
    public Integer saveUser(Usuario user) {
        String passwd= user.getPassword();
        String encodedPasswod = passwordEncoder.encode(passwd);
        user.setPassword(encodedPasswod);
        user = usuarioRepository.save(user);
        return (int) user.getId();
    }

    public UserDetails loadUserByUsername(String emilio) throws UsernameNotFoundException {

        Optional<Usuario> opt = usuarioRepository.findByEmail(emilio);

        if(opt.isEmpty()){
            throw new UsernameNotFoundException("User with email: " +emilio +" not found !");
        } else {
            Usuario user = opt.get();
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    List.of(new SimpleGrantedAuthority("ADMIN"))
            );
            
        }

    }
    @Override
    public void delete(Usuario usuario) {
        usuarioRepository.delete(usuario);
    }


    @Scheduled(cron = "0 0 0 * * *") // Ejecuta a medianoche todos los días
    @Transactional // Asegura que la operación es transaccional
    public void borrarUsuariosMarcados() throws MessagingException {
        List<Usuario> usuarios = usuarioRepository.findAllByBorrandoTrue();
        LocalDateTime now = LocalDateTime.now();
        System.out.println("HOLAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        for (Usuario usuario : usuarios) {
            Duration duration = Duration.between(usuario.getFechaBorrado(), now); 

            if (duration.toDays() >= 2) {

                System.out.println("ANTES DEL EMAIL");
                // Manda un email al usuario
                EmailDTO email = new EmailDTO();                
                email.setAsunto("Tu cuenta ha sido eliminada");
                email.setDestinatario(usuario.getEmail());
                email.setMensaje("Hola, tu cuenta ha sido eliminada conforme a tu solicitud.");
                emailService.sendMail(email);

                System.out.println("DESPUES DEL EMAIL");

                // Cierra la sesión del usuario
                cerrarSesion(usuario);

                System.out.println("BORRANDO EL CARRITO");
                // Elimina el carrito
                if (usuario.getCarrito() != null) {
                    usuario.getCarrito().setProductos(null); // Elimina los productos del carrito
                    carritoRepository.delete(usuario.getCarrito()); // Elimina el carrito
                }


                System.out.println("BORRANDO LOS PEDIDOS");
                // Elimina los pedidos y sus productos
                for (Pedido pedido : usuario.getPedidos()) {
                    pedido.getProductos().forEach(producto -> producto.setPedido(null));
                    pedido.setUsuario(null);
                    pedido.setProductos(null); // Elimina los productos del pedido
                    pedidoRepository.delete(pedido); // Elimina el pedido
                }
                

                System.out.println("BORRANDO EL USUARIO");
                // Elimina el usuario
                usuarioRepository.delete(usuario);
            }
        }
    }


    @SuppressWarnings("null")
    private void cerrarSesion(Usuario usuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            if (userDetails.getUsername().equals(usuario.getEmail())) {
                // Usar SecurityContextLogoutHandler para manejar el cierre de sesión
                SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
                logoutHandler.logout(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest(), null, authentication);
            }
        }
    }

}
