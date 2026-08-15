package com.bakery.controller;

import com.bakery.service.CakeService;
import com.bakery.service.FiestaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductosController {

    @Autowired
    private CakeService cakeService;

    @Autowired
    private FiestaService fiestaService;

    // ─── CATÁLOGO DE CAKES ───
    @GetMapping("/cakes")
    public String cakes(Model model) {
        model.addAttribute("pagina", "cakes");
        model.addAttribute("canonicalUrl", "https://elfenixbakery.com/cakes");
        model.addAttribute("cakes", cakeService.obtenerTodos());
        return "public/cakes";
    }

    @GetMapping("/cakes/{slug}")
    public String cakeDetalle(@PathVariable String slug, Model model) {
        return cakeService.obtenerPorSlug(slug).map(c -> {
            model.addAttribute("pagina", "cakes");
            model.addAttribute("canonicalUrl", "https://elfenixbakery.com/cakes/" + c.getSlug());
            model.addAttribute("cake", c);
            model.addAttribute("relacionados",
                cakeService.obtenerPorCategoria(c.getCatSlug()).stream()
                    .filter(r -> !r.getSlug().equals(slug))
                    .limit(3).toList());
            return "public/cake-detalle";
        }).orElse("redirect:/cakes");
    }

    // ─── MENÚ PARA FIESTAS ───
    @GetMapping("/fiestas")
    public String fiestas(Model model) {
        model.addAttribute("pagina", "fiestas");
        model.addAttribute("canonicalUrl", "https://elfenixbakery.com/fiestas");
        model.addAttribute("productos", fiestaService.obtenerTodos());
        return "public/fiestas";
    }

    @GetMapping("/fiestas/{slug}")
    public String fiestaDetalle(@PathVariable String slug, Model model) {
        return fiestaService.obtenerPorSlug(slug).map(p -> {
            model.addAttribute("pagina", "fiestas");
            model.addAttribute("canonicalUrl", "https://elfenixbakery.com/fiestas/" + p.getSlug());
            model.addAttribute("producto", p);
            return "public/fiesta-detalle";
        }).orElse("redirect:/fiestas");
    }
}
