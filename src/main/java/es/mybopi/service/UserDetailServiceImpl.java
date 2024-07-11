package es.mybopi.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import es.mybopi.model.Usuario;
import jakarta.servlet.http.HttpSession;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    HttpSession session;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Usuario> user = usuarioService.findByEmail(username);

        return user.map(usuario -> {
            System.out.println("Usuario encontrado: " + usuario.getNombre());
            return User.builder()
                    .username(usuario.getNombre())
                    .password(encoder.encode(usuario.getPassword()))
                    .roles(usuario.getRoles())
                    .build();
        }).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
