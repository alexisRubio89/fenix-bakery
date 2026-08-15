package com.bakery.controller;

import com.bakery.service.CakeService;
import com.bakery.service.EspecialidadService;
import com.bakery.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/especialidades")
public class EspecialidadAdminController {

    @Autowired private EspecialidadService especialidadService;
    @Autowired private MenuService menuService;
    @Autowired private CakeService cakeService;

    @GetMapping
    public String editar(Model model) {
        model.addAttribute("especialidades", especialidadService.obtenerParaEditar());
        model.addAttribute("productosMenu", menuService.obtenerTodos());
        model.addAttribute("cakes", cakeService.obtenerTodos());
        return "admin/especialidades";
    }

    @PostMapping
    public String guardar(@RequestParam("valor") List<String> valores,
                          RedirectAttributes ra) {
        especialidadService.guardar(valores);
        ra.addFlashAttribute("mensaje", "Especialidades actualizadas correctamente.");
        return "redirect:/admin/especialidades";
    }
}
