package es.mybopi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import es.mybopi.model.Usuario;
import es.mybopi.repository.UsuarioRepository;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Usuario> user = userRepository.findByEmail(username);

        if (user.isPresent()) {
            var userObj = user.get();
            
            List<GrantedAuthority> authorities = new ArrayList<>();
            for (String role : getRoles(userObj)) {
                authorities.add(new SimpleGrantedAuthority(role));
            }

            return new User(userObj.getEmail(), userObj.getPassword(), authorities);
        } else {
            throw new UsernameNotFoundException(username);
        }
    }

    private String[] getRoles(Usuario user) {
        if (user.getRoles() == null) {
            return new String[]{"USER"};
        }

        return user.getRoles().split(",");
    }
}
