package com.bakery.controller;

import com.bakery.model.Cake;
import com.bakery.service.CakeService;
import com.bakery.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/cakes")
public class CakeAdminController {

    @Autowired
    private CakeService cakeService;

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
        model.addAttribute("cakes", cakeService.obtenerTodos());
        return "admin/cake-lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cake", new Cake());
        model.addAttribute("esNuevo", true);
        return "admin/cake-form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return cakeService.obtenerPorId(id).map(cake -> {
            model.addAttribute("cake", cake);
            model.addAttribute("esNuevo", false);
            return "admin/cake-form";
        }).orElseGet(() -> {
            flash.addFlashAttribute("mensaje", "Cake no encontrado.");
            flash.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/cakes";
        });
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Cake cake,
                          @RequestParam(value = "imagenesConservadas", required = false) List<String> imagenesConservadas,
                          @RequestParam(value = "fotosNuevas", required = false) List<MultipartFile> fotosNuevas,
                          @RequestParam(value = "videoArchivo", required = false) MultipartFile videoArchivo,
                          @RequestParam(value = "eliminarVideo", required = false) String eliminarVideo,
                          RedirectAttributes flash) {
        boolean esNuevo = (cake.getId() == null);
        if (cake.getSlug() == null || cake.getSlug().isBlank()) {
            cake.setSlug(generarSlug(cake.getNombre()));
        }

        // Lista final de imágenes: las conservadas + las nuevas subidas a S3
        List<String> imagenesFinal = new ArrayList<>();
        if (imagenesConservadas != null) {
            imagenesFinal.addAll(imagenesConservadas);
        }

        // Si está editando, detectar fotos eliminadas para borrarlas de S3
        if (!esNuevo) {
            cakeService.obtenerPorId(cake.getId()).ifPresent(viejo -> {
                for (String urlVieja : viejo.getImagenes()) {
                    boolean seConserva = imagenesConservadas != null && imagenesConservadas.contains(urlVieja);
                    if (!seConserva && urlVieja.contains(".amazonaws.com/")) {
                        s3Service.eliminarArchivo(urlVieja);
                    }
                }
            });
        }

        // Subir fotos nuevas
        if (fotosNuevas != null) {
            for (MultipartFile foto : fotosNuevas) {
                if (foto != null && !foto.isEmpty()) {
                    try {
                        String url = s3Service.subirArchivo(foto, "cakes");
                        imagenesFinal.add(url);
                    } catch (Exception e) {
                        flash.addFlashAttribute("mensaje", "Algunas fotos no se pudieron subir: " + e.getMessage());
                        flash.addFlashAttribute("tipoMensaje", "warning");
                    }
                }
            }
        }

        cake.setImagenesList(imagenesFinal);

        // ── Video ──────────────────────────────────
        // Si marcó eliminar el video actual
        if ("true".equals(eliminarVideo) && cake.getVideo() != null && cake.getVideo().contains(".amazonaws.com/")) {
            s3Service.eliminarArchivo(cake.getVideo());
            cake.setVideo(null);
        }
        // Si subió un video nuevo
        if (videoArchivo != null && !videoArchivo.isEmpty()) {
            try {
                String videoViejo = cake.getVideo();
                String url = s3Service.subirArchivo(videoArchivo, "cakes-videos");
                cake.setVideo(url);
                if (videoViejo != null && videoViejo.contains(".amazonaws.com/")) {
                    s3Service.eliminarArchivo(videoViejo);
                }
            } catch (Exception e) {
                flash.addFlashAttribute("mensaje", "Cake guardado, pero el video no se pudo subir: " + e.getMessage());
                flash.addFlashAttribute("tipoMensaje", "warning");
            }
        }

        cakeService.guardar(cake);

        if (!flash.getFlashAttributes().containsKey("mensaje")) {
            flash.addFlashAttribute("mensaje", esNuevo ? "Cake creado." : "Cake actualizado.");
            flash.addFlashAttribute("tipoMensaje", "success");
        }
        return "redirect:/admin/cakes";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        // Borrar también las fotos y el video de S3
        cakeService.obtenerPorId(id).ifPresent(cake -> {
            for (String url : cake.getImagenes()) {
                if (url.contains(".amazonaws.com/")) {
                    s3Service.eliminarArchivo(url);
                }
            }
            if (cake.getVideo() != null && cake.getVideo().contains(".amazonaws.com/")) {
                s3Service.eliminarArchivo(cake.getVideo());
            }
        });
        cakeService.eliminar(id);
        flash.addFlashAttribute("mensaje", "Cake eliminado.");
        flash.addFlashAttribute("tipoMensaje", "warning");
        return "redirect:/admin/cakes";
    }

    private String generarSlug(String nombre) {
        if (nombre == null) return "cake-" + System.currentTimeMillis();
        return nombre.toLowerCase()
            .replace("á","a").replace("é","e").replace("í","i")
            .replace("ó","o").replace("ú","u").replace("ñ","n")
            .replaceAll("[^a-z0-9\\s-]", "")
            .trim().replaceAll("\\s+", "-");
    }
}
