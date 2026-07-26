package com.bakery.controller;

import com.bakery.model.Fiesta;
import com.bakery.service.FiestaService;
import com.bakery.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/fiestas")
public class FiestaAdminController {

    @Autowired
    private FiestaService fiestaService;

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
        model.addAttribute("productos", fiestaService.obtenerTodos());
        return "admin/fiesta-lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("fiesta", new Fiesta());
        model.addAttribute("esNuevo", true);
        return "admin/fiesta-form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return fiestaService.obtenerPorId(id).map(fiesta -> {
            model.addAttribute("fiesta", fiesta);
            model.addAttribute("esNuevo", false);
            return "admin/fiesta-form";
        }).orElseGet(() -> {
            flash.addFlashAttribute("mensaje", "Producto no encontrado.");
            flash.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/fiestas";
        });
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Fiesta fiesta,
                          @RequestParam(value = "foto", required = false) MultipartFile foto,
                          @RequestParam(value = "videoArchivo", required = false) MultipartFile videoArchivo,
                          @RequestParam(value = "eliminarVideo", required = false) String eliminarVideo,
                          RedirectAttributes flash) {
        boolean esNuevo = (fiesta.getId() == null);
        if (fiesta.getSlug() == null || fiesta.getSlug().isBlank()) {
            fiesta.setSlug(generarSlug(fiesta.getNombre()));
        }

        if (foto != null && !foto.isEmpty()) {
            try {
                String fotoVieja = fiesta.getImagen();
                String url = s3Service.subirArchivo(foto, "fiestas");
                fiesta.setImagen(url);
                if (fotoVieja != null && fotoVieja.contains(".amazonaws.com/")) {
                    s3Service.eliminarArchivo(fotoVieja);
                }
            } catch (Exception e) {
                flash.addFlashAttribute("mensaje", "Producto guardado, pero la foto no se pudo subir: " + e.getMessage());
                flash.addFlashAttribute("tipoMensaje", "warning");
                fiestaService.guardar(fiesta);
                return "redirect:/admin/fiestas";
            }
        }

        // Video: eliminar el actual si se marcó
        if ("true".equals(eliminarVideo) && fiesta.getVideo() != null && fiesta.getVideo().contains(".amazonaws.com/")) {
            s3Service.eliminarArchivo(fiesta.getVideo());
            fiesta.setVideo(null);
        }
        // Subir video nuevo
        if (videoArchivo != null && !videoArchivo.isEmpty()) {
            try {
                String videoViejo = fiesta.getVideo();
                String url = s3Service.subirArchivo(videoArchivo, "fiestas-videos");
                fiesta.setVideo(url);
                if (videoViejo != null && videoViejo.contains(".amazonaws.com/")) {
                    s3Service.eliminarArchivo(videoViejo);
                }
            } catch (Exception e) {
                flash.addFlashAttribute("mensaje", "Producto guardado, pero el video no se pudo subir: " + e.getMessage());
                flash.addFlashAttribute("tipoMensaje", "warning");
                fiestaService.guardar(fiesta);
                return "redirect:/admin/fiestas";
            }
        }

        fiestaService.guardar(fiesta);
        flash.addFlashAttribute("mensaje", esNuevo ? "Producto creado." : "Producto actualizado.");
        flash.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/admin/fiestas";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        fiestaService.obtenerPorId(id).ifPresent(fiesta -> {
            if (fiesta.getImagen() != null && fiesta.getImagen().contains(".amazonaws.com/")) {
                s3Service.eliminarArchivo(fiesta.getImagen());
            }
            if (fiesta.getVideo() != null && fiesta.getVideo().contains(".amazonaws.com/")) {
                s3Service.eliminarArchivo(fiesta.getVideo());
            }
        });
        fiestaService.eliminar(id);
        flash.addFlashAttribute("mensaje", "Producto eliminado.");
        flash.addFlashAttribute("tipoMensaje", "warning");
        return "redirect:/admin/fiestas";
    }

    private String generarSlug(String nombre) {
        if (nombre == null) return "fiesta-" + System.currentTimeMillis();
        return nombre.toLowerCase()
            .replace("á","a").replace("é","e").replace("í","i")
            .replace("ó","o").replace("ú","u").replace("ñ","n")
            .replaceAll("[^a-z0-9\\s-]", "")
            .trim().replaceAll("\\s+", "-");
    }
}
