package com.bakery.controller;

import com.bakery.model.MenuItem;
import com.bakery.service.MenuService;
import com.bakery.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/menu")
public class MenuAdminController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private com.bakery.service.MensajeContactoService mensajeService;

    @ModelAttribute("sidebarNoLeidos")
    public long sidebarNoLeidos() {
        return mensajeService.contarNoLeidos();
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("items", menuService.obtenerTodos());
        return "admin/menu-lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("item", new MenuItem());
        model.addAttribute("esNuevo", true);
        return "admin/menu-form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return menuService.obtenerPorId(id).map(item -> {
            model.addAttribute("item", item);
            model.addAttribute("esNuevo", false);
            return "admin/menu-form";
        }).orElseGet(() -> {
            flash.addFlashAttribute("mensaje", "Producto no encontrado.");
            flash.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/menu";
        });
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute MenuItem item,
                          @RequestParam(value = "foto", required = false) MultipartFile foto,
                          @RequestParam(value = "videoArchivo", required = false) MultipartFile video,
                          @RequestParam(value = "eliminarVideo", required = false) String eliminarVideo,
                          RedirectAttributes flash) {
        boolean esNuevo = (item.getId() == null);
        if (item.getSlug() == null || item.getSlug().isBlank()) {
            item.setSlug(generarSlug(item.getNombre()));
        }

        // Si subieron una foto nueva, subirla a S3 y guardar su URL
        if (foto != null && !foto.isEmpty()) {
            try {
                // Si está editando y ya tenía una foto en S3, borrar la vieja
                String fotoVieja = item.getImagen();
                String url = s3Service.subirArchivo(foto, "menu");
                item.setImagen(url);
                if (fotoVieja != null && fotoVieja.contains(".amazonaws.com/")) {
                    s3Service.eliminarArchivo(fotoVieja);
                }
            } catch (Exception e) {
                flash.addFlashAttribute("mensaje", "Producto guardado, pero la foto no se pudo subir: " + e.getMessage());
                flash.addFlashAttribute("tipoMensaje", "warning");
                menuService.guardar(item);
                return "redirect:/admin/menu";
            }
        }

        // Si marcó "eliminar video", borrarlo de S3 y limpiar el campo
        if ("true".equals(eliminarVideo) && item.getVideo() != null && item.getVideo().contains(".amazonaws.com/")) {
            s3Service.eliminarArchivo(item.getVideo());
            item.setVideo(null);
        }

        // Si subieron un video nuevo, subirlo a S3
        if (video != null && !video.isEmpty()) {
            try {
                String videoViejo = item.getVideo();
                String url = s3Service.subirArchivo(video, "menu-videos");
                item.setVideo(url);
                if (videoViejo != null && videoViejo.contains(".amazonaws.com/")) {
                    s3Service.eliminarArchivo(videoViejo);
                }
            } catch (Exception e) {
                flash.addFlashAttribute("mensaje", "Producto guardado, pero el video no se pudo subir: " + e.getMessage());
                flash.addFlashAttribute("tipoMensaje", "warning");
                menuService.guardar(item);
                return "redirect:/admin/menu";
            }
        }

        menuService.guardar(item);
        flash.addFlashAttribute("mensaje", esNuevo ? "Producto creado." : "Producto actualizado.");
        flash.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/admin/menu";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        menuService.obtenerPorId(id).ifPresent(item -> {
            if (item.getImagen() != null && item.getImagen().contains(".amazonaws.com/")) {
                s3Service.eliminarArchivo(item.getImagen());
            }
            if (item.getVideo() != null && item.getVideo().contains(".amazonaws.com/")) {
                s3Service.eliminarArchivo(item.getVideo());
            }
        });
        menuService.eliminar(id);
        flash.addFlashAttribute("mensaje", "Producto eliminado.");
        flash.addFlashAttribute("tipoMensaje", "warning");
        return "redirect:/admin/menu";
    }

    private String generarSlug(String nombre) {
        if (nombre == null) return "producto-" + System.currentTimeMillis();
        return nombre.toLowerCase()
            .replace("á","a").replace("é","e").replace("í","i")
            .replace("ó","o").replace("ú","u").replace("ñ","n")
            .replaceAll("[^a-z0-9\\s-]", "")
            .trim().replaceAll("\\s+", "-");
    }
}
