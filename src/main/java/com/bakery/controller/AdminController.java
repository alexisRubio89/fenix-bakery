package com.bakery.controller;

import com.bakery.model.Producto;
import com.bakery.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private com.bakery.service.MensajeContactoService mensajeService;

    // Disponible en todas las vistas admin (para el badge del sidebar)
    @ModelAttribute("sidebarNoLeidos")
    public long sidebarNoLeidos() {
        return mensajeService.contarNoLeidos();
    }

    // ── Dashboard ─────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalProductos", productoService.totalProductos());
        model.addAttribute("stockBajo", productoService.stockBajo());
        model.addAttribute("valorInventario", productoService.valorTotalInventario());
        model.addAttribute("porCategoria", productoService.productosPorCategoria());
        return "admin/dashboard";
    }

    // ── Inventario (lista) ─────────────────────────
    @GetMapping("/inventario")
    public String inventario(Model model,
                             @RequestParam(required = false) String buscar,
                             @RequestParam(required = false) String categoria) {
        if (buscar != null && !buscar.isBlank()) {
            model.addAttribute("productos", productoService.buscarPorNombre(buscar));
            model.addAttribute("buscar", buscar);
        } else if (categoria != null && !categoria.isBlank()) {
            model.addAttribute("productos", productoService.buscarPorCategoria(categoria));
            model.addAttribute("categoriaActiva", categoria);
        } else {
            model.addAttribute("productos", productoService.obtenerTodos());
        }
        return "admin/inventario";
    }

    // ── Formulario nuevo producto ──────────────────
    @GetMapping("/inventario/nuevo")
    public String formNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("accion", "Agregar");
        return "admin/formulario";
    }

    // ── Guardar nuevo producto ─────────────────────
    @PostMapping("/inventario/guardar")
    public String guardar(@Valid @ModelAttribute Producto producto,
                          BindingResult result,
                          Model model,
                          RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("accion", producto.getId() == null ? "Agregar" : "Editar");
            return "admin/formulario";
        }
        productoService.guardar(producto);
        flash.addFlashAttribute("mensaje", "Producto guardado correctamente.");
        flash.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/admin/inventario";
    }

    // ── Formulario editar producto ─────────────────
    @GetMapping("/inventario/editar/{id}")
    public String formEditar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return productoService.obtenerPorId(id).map(p -> {
            model.addAttribute("producto", p);
            model.addAttribute("accion", "Editar");
            return "admin/formulario";
        }).orElseGet(() -> {
            flash.addFlashAttribute("mensaje", "Producto no encontrado.");
            flash.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/inventario";
        });
    }

    // ── Eliminar producto ─────────────────────────
    @PostMapping("/inventario/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        productoService.eliminar(id);
        flash.addFlashAttribute("mensaje", "Producto eliminado.");
        flash.addFlashAttribute("tipoMensaje", "warning");
        return "redirect:/admin/inventario";
    }

    // ── Mensajes de contacto (lista) ───────────────
    @GetMapping("/mensajes")
    public String mensajes(Model model) {
        model.addAttribute("mensajes", mensajeService.obtenerTodos());
        model.addAttribute("noLeidos", mensajeService.contarNoLeidos());
        return "admin/mensajes";
    }

    // ── Marcar leído / no leído ────────────────────
    @PostMapping("/mensajes/leido/{id}")
    public String marcarLeido(@PathVariable Long id,
                              @RequestParam(defaultValue = "true") boolean valor,
                              RedirectAttributes flash) {
        mensajeService.marcarLeido(id, valor);
        return "redirect:/admin/mensajes";
    }

    // ── Marcar leído por AJAX (al expandir, sin recargar) ──
    @PostMapping("/mensajes/leido-ajax/{id}")
    @ResponseBody
    public String marcarLeidoAjax(@PathVariable Long id) {
        mensajeService.marcarLeido(id, true);
        return "{\"ok\":true,\"noLeidos\":" + mensajeService.contarNoLeidos() + "}";
    }

    // ── Marcar seleccionados como leídos ───────────
    @PostMapping("/mensajes/leidos-lote")
    public String marcarLeidosLote(@RequestParam(name = "ids", required = false) java.util.List<Long> ids,
                                   RedirectAttributes flash) {
        if (ids != null && !ids.isEmpty()) {
            mensajeService.marcarLeidosEnLote(ids);
            flash.addFlashAttribute("mensaje", ids.size() + " mensaje(s) marcado(s) como leído(s).");
            flash.addFlashAttribute("tipoMensaje", "success");
        }
        return "redirect:/admin/mensajes";
    }

    // ── Eliminar seleccionados ─────────────────────
    @PostMapping("/mensajes/eliminar-lote")
    public String eliminarLote(@RequestParam(name = "ids", required = false) java.util.List<Long> ids,
                               RedirectAttributes flash) {
        if (ids != null && !ids.isEmpty()) {
            mensajeService.eliminarEnLote(ids);
            flash.addFlashAttribute("mensaje", ids.size() + " mensaje(s) eliminado(s).");
            flash.addFlashAttribute("tipoMensaje", "warning");
        }
        return "redirect:/admin/mensajes";
    }

    // ── Eliminar todos ─────────────────────────────
    @PostMapping("/mensajes/eliminar-todos")
    public String eliminarTodos(RedirectAttributes flash) {
        mensajeService.eliminarTodos();
        flash.addFlashAttribute("mensaje", "Todos los mensajes fueron eliminados.");
        flash.addFlashAttribute("tipoMensaje", "warning");
        return "redirect:/admin/mensajes";
    }

    // ── Eliminar mensaje ───────────────────────────
    @PostMapping("/mensajes/eliminar/{id}")
    public String eliminarMensaje(@PathVariable Long id, RedirectAttributes flash) {
        mensajeService.eliminar(id);
        flash.addFlashAttribute("mensaje", "Mensaje eliminado.");
        flash.addFlashAttribute("tipoMensaje", "warning");
        return "redirect:/admin/mensajes";
    }
}
