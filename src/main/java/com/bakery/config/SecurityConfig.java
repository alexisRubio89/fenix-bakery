package com.bakery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Credenciales leídas de variables de entorno.
    // En local, si no se definen, usan los valores por defecto.
    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @Value("${gerente.username:}")
    private String gerenteUsername;

    @Value("${gerente.password:}")
    private String gerentePassword;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/menu", "/menu/**", "/nosotros", "/contacto").permitAll()
                .requestMatchers("/cakes", "/cakes/**", "/fiestas", "/fiestas/**").permitAll()
                .requestMatchers("/locaciones/**").permitAll()
                .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/sitemap.xml", "/robots.txt").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        List<UserDetails> usuarios = new ArrayList<>();

        // Usuario admin (siempre presente)
        usuarios.add(User.builder()
                .username(adminUsername)
                .password(passwordEncoder().encode(adminPassword))
                .roles("ADMIN").build());

        // Usuario gerente (solo si se definieron sus variables)
        if (gerenteUsername != null && !gerenteUsername.isBlank()
                && gerentePassword != null && !gerentePassword.isBlank()) {
            usuarios.add(User.builder()
                    .username(gerenteUsername)
                    .password(passwordEncoder().encode(gerentePassword))
                    .roles("ADMIN").build());
        }

        return new InMemoryUserDetailsManager(usuarios);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
