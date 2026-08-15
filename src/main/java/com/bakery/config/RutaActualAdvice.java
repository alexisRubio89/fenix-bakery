package com.bakery.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expone la ruta actual (sin query string) a todas las plantillas.
 * Se usa para el selector de idioma y las etiquetas hreflang, de modo que
 * cambiar de idioma mantenga al visitante en la misma página.
 */
@ControllerAdvice
public class RutaActualAdvice {

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return (uri == null || uri.isBlank()) ? "/" : uri;
    }
}
