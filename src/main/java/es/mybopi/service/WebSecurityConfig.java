package es.mybopi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                        request -> request
                                .requestMatchers("/", "/usuario/login", "/usuario/registro", "/usuario/save", "/usuario/cerrar", "/images/**", "/producto/**", "/totebags/**", "/mochilas/**", "/buscar/**").permitAll()
                                .requestMatchers("/productos/**", "/admin/**").hasRole("ADMIN")
                                .requestMatchers("/pedidos/**").hasRole("USER")
                                .anyRequest().authenticated()
                )
                .formLogin(
                        formLogin -> formLogin
                                .loginPage("/usuario/login")
                                .defaultSuccessUrl("/", true)
                                .failureUrl("/usuario/login")
                )
                .logout(logout -> logout
                                .logoutUrl("/usuario/cerrar")
                                .logoutSuccessUrl("/")
                )
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