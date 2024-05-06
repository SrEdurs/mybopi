package es.mybopi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import es.mybopi.model.Usuario;
import es.mybopi.repository.UsuarioRepository;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

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

}
