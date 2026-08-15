package com.bakery.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security 6 retrasa la generación del token CSRF hasta que algo
 * lo pide explícitamente (ej. ${_csrf.token} en un template Thymeleaf).
 * Si eso pasa después de que la respuesta ya se empezó a enviar (páginas
 * largas que superan el buffer de Tomcat), falla con:
 * "Cannot create a session after the response has been committed".
 *
 * Este filtro fuerza la resolución del token al inicio de cada petición,
 * antes de que se escriba una sola línea de la respuesta.
 */
@Component
public class CsrfEarlyLoadFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken(); // fuerza la creación de sesión/token ahora, no más tarde
        }
        filterChain.doFilter(request, response);
    }
}
