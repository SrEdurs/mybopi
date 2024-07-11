package es.mybopi.service;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/vendor/**", "/css/**");
    }

    @Bean
    UserDetailsService userDetailsService() {
        return myUserDetailsService;
    }

    @Bean
    public AuthenticationSuccessHandler myAuthenticationSuccessHandler() {
        return new SimpleUrlAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                    Authentication authentication) throws IOException, ServletException {
                HttpSession session = request.getSession();
                if (session.getAttribute("mensaje") == null) {
                    session.setAttribute("mensaje", "Has iniciado sesión");
                }
                setDefaultTargetUrl("/");
                super.onAuthenticationSuccess(request, response, authentication);
            }
        };
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                        request -> request
                                .requestMatchers("/", "/usuario/login", "/usuario/registro", "/usuario/save",
                                        "/usuario/cerrar", "/images/**", "/producto/**", "/totebags/**", "/mochilas/**",
                                        "/buscar/**",
                                        "/send-email", "/usuario/recordar", "/usuario/recordar/**",
                                        "/usuario/cambiapassword",
                                        "/usuario/cambiapassword/**")
                                .permitAll()
                                .requestMatchers("/productos/**", "/admin/**").hasRole("ADMIN")
                                .requestMatchers("/pedidos/**").hasRole("USER")
                                .anyRequest().authenticated())
                .formLogin(
                        formLogin -> formLogin
                                .loginPage("/usuario/login")
                                .successHandler(myAuthenticationSuccessHandler())
                                .failureUrl("/usuario/login?error=true"))
                .logout(logout -> logout
                        .logoutUrl("/usuario/cerrar")
                        .logoutSuccessUrl("/usuario/login?logout=true"))
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
}