package es.mybopi.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.core.userdetails.User.withUsername;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {



    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers("/vendor/**", "/css/**");
    }

    @Bean
    InMemoryUserDetailsManager userDetailsService() {
    UserDetails user = withUsername("Admin")
      .username("Admin")
      .password("admin")
      .roles("USER")
      .build();

    return new InMemoryUserDetailsManager(user);
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        .authorizeHttpRequests(
            request -> request
                .requestMatchers("/", "/usuario/login", "/usuario/registro", "/usuario/save", "/usuario/cerrar", "/usuario/acceder").permitAll()
                .anyRequest().authenticated()
        )
        .formLogin(
            formLogin -> formLogin
            .loginPage("/usuario/login")
            .defaultSuccessUrl("/", true)
            .failureUrl("/usuario/login")
        )
        .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

   /*  @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    } */

}