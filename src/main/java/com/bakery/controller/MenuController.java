package com.bakery.controller;

import com.bakery.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/menu")
    public String menu(Model model) {
        model.addAttribute("pagina", "menu");
        model.addAttribute("canonicalUrl", "https://elfenixbakery.com/menu");
        model.addAttribute("productos", menuService.obtenerTodos());
        return "public/menu";
    }

    @GetMapping("/menu/{slug}")
    public String detalle(@PathVariable String slug, Model model) {
        return menuService.obtenerPorSlug(slug).map(p -> {
            model.addAttribute("pagina", "menu");
            model.addAttribute("canonicalUrl", "https://elfenixbakery.com/menu/" + p.getSlug());
            model.addAttribute("producto", p);
            model.addAttribute("relacionados",
                menuService.obtenerPorCategoria(p.getCatSlug()).stream()
                    .filter(r -> !r.getSlug().equals(slug))
                    .limit(3).toList());
            return "public/producto-detalle";
        }).orElse("redirect:/menu");
    }
}
