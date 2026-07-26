package com.bakery.controller;

import com.bakery.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PublicController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private com.bakery.service.MensajeContactoService mensajeService;

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("pagina", "inicio");
        model.addAttribute("metaTitle", "Fenix Bakery — Panadería y Dulcería Cubana en New Jersey");
        model.addAttribute("metaDescription", "Auténtica panadería cubana en New Jersey. Pan cubano, croquetas, pasteles, cakes y postres tradicionales horneados frescos cada día en North Bergen, West New York y Union City.");
        model.addAttribute("canonicalUrl", "https://www.fenixbakery.com/");
        return "public/inicio";
    }

    @GetMapping("/nosotros")
    public String nosotros(Model model) {
        model.addAttribute("pagina", "nosotros");
        model.addAttribute("metaTitle", "Nuestra Historia | Fenix Bakery — Panadería Cubana NJ");
        model.addAttribute("metaDescription", "Conoce la historia de Fenix Bakery: de La Habana a New Jersey. Tradición cubana, recetas auténticas y pasión por la repostería desde hace más de 30 años.");
        model.addAttribute("canonicalUrl", "https://www.fenixbakery.com/nosotros");
        return "public/nosotros";
    }

    @GetMapping("/contacto")
    public String contacto(Model model) {
        model.addAttribute("pagina", "contacto");
        model.addAttribute("metaTitle", "Contacto y Locaciones | Fenix Bakery NJ");
        model.addAttribute("metaDescription", "Visítanos en Union City, West New York o North Bergen. Encuentra horarios, teléfonos y la locación más cercana de Fenix Bakery en New Jersey.");
        model.addAttribute("canonicalUrl", "https://www.fenixbakery.com/contacto");
        return "public/contacto";
    }

    @PostMapping("/contacto")
    public String enviarContacto(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam String asunto,
            @RequestParam String mensaje,
            Model model) {
        model.addAttribute("pagina", "contacto");
        try {
            // Guardar en base de datos (con filtro de groserías aplicado en el servicio)
            mensajeService.guardar(nombre, email, telefono, asunto, mensaje);
            // Enviar también por email
            emailService.enviarMensajeContacto(nombre, email, telefono, asunto, mensaje);
            model.addAttribute("enviado", true);
        } catch (Exception e) {
            model.addAttribute("error", true);
        }
        return "public/contacto";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}

