package com.bakery.controller;

import com.bakery.model.Producto;
import com.bakery.model.TipoProducto;
import com.bakery.model.UnidadMedida;
import com.bakery.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private com.bakery.service.MensajeContactoService mensajeService;

    @Autowired
    private com.bakery.service.ExcelInventarioService excelInventarioService;

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
                             @RequestParam(required = false) TipoProducto tipo,
                             @RequestParam(required = false) String buscar,
                             @RequestParam(required = false) String categoria,
                             @RequestParam(required = false) Boolean soloStockBajo) {

        List<Producto> productos;

        if (Boolean.TRUE.equals(soloStockBajo)) {
            productos = productoService.stockBajo(tipo);
            model.addAttribute("soloStockBajo", true);
        } else if (buscar != null && !buscar.isBlank()) {
            productos = productoService.buscarPorNombre(tipo, buscar);
            model.addAttribute("buscar", buscar);
        } else if (categoria != null && !categoria.isBlank()) {
            productos = productoService.buscarPorCategoria(tipo, categoria);
            model.addAttribute("categoriaActiva", categoria);
        } else if (tipo != null) {
            productos = productoService.obtenerPorTipo(tipo);
        } else {
            productos = productoService.obtenerTodos();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("tipoActivo", tipo);
        // Chips de categoría: los del tipo activo, o todos si no hay tipo seleccionado
        model.addAttribute("categorias", tipo != null
                ? productoService.categoriasDe(tipo)
                : todasLasCategorias());
        // Contadores de las pestañas
        model.addAttribute("totalIngredientes", productoService.totalPorTipo(TipoProducto.INGREDIENTE));
        model.addAttribute("totalInsumos", productoService.totalPorTipo(TipoProducto.INSUMO));
        model.addAttribute("totalGeneral", productoService.totalProductos());
        model.addAttribute("totalStockBajo", productoService.contarStockBajo());
        return "admin/inventario";
    }

    private List<String> todasLasCategorias() {
        List<String> todas = new ArrayList<>(ProductoService.CATEGORIAS_INGREDIENTE);
        todas.addAll(ProductoService.CATEGORIAS_INSUMO);
        return todas;
    }

    /** Opciones de los desplegables del formulario, en un solo sitio. */
    private void cargarOpciones(Model model) {
        model.addAttribute("tipos", TipoProducto.values());
        model.addAttribute("unidades", UnidadMedida.values());
        model.addAttribute("categoriasIngrediente", ProductoService.CATEGORIAS_INGREDIENTE);
        model.addAttribute("categoriasInsumo", ProductoService.CATEGORIAS_INSUMO);
    }

    // ── Formulario nuevo producto ──────────────────
    @GetMapping("/inventario/nuevo")
    public String formNuevo(Model model, @RequestParam(required = false) TipoProducto tipo) {
        Producto p = new Producto();
        p.setTipo(tipo != null ? tipo : TipoProducto.INGREDIENTE);
        model.addAttribute("producto", p);
        model.addAttribute("accion", "Agregar");
        cargarOpciones(model);
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
            cargarOpciones(model);
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
            cargarOpciones(model);
            return "admin/formulario";
        }).orElseGet(() -> {
            flash.addFlashAttribute("mensaje", "Producto no encontrado.");
            flash.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/inventario";
        });
    }

    /**
     * Descarga el reporte de almacén en Excel: existencia de los insumos en la
     * primera hoja y lo que hay que reponer en la segunda. Los ingredientes se
     * llevan aparte y no entran en este reporte.
     */
    @GetMapping("/inventario/exportar")
    public ResponseEntity<byte[]> exportarExcel() {
        try {
            List<Producto> insumos = productoService.obtenerPorTipo(TipoProducto.INSUMO);

            byte[] excel = excelInventarioService.generar(insumos);
            String archivo = "Insumos-Almacen-"
                    + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", archivo);
            headers.setCacheControl("no-cache, no-store, must-revalidate");

            return new ResponseEntity<>(excel, headers, HttpStatus.OK);

        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Ajuste rápido de existencia desde la propia lista, sin abrir el formulario:
     * es lo que más se usa al recibir mercancía o al cerrar el día.
     * Los productos con desglose reciben cajas y sueltas por separado.
     */
    @PostMapping("/inventario/ajustar/{id}")
    public String ajustarCantidad(@PathVariable Long id,
                                  @RequestParam(required = false) BigDecimal cajas,
                                  @RequestParam(required = false) BigDecimal sueltas,
                                  RedirectAttributes flash) {
        productoService.obtenerPorId(id).ifPresentOrElse(p -> {
            if (esNegativo(cajas) || esNegativo(sueltas)) {
                flash.addFlashAttribute("mensaje", "La cantidad no puede ser negativa.");
                flash.addFlashAttribute("tipoMensaje", "error");
                return;
            }
            p.asignarExistencia(cajas, sueltas);
            productoService.guardar(p);
            flash.addFlashAttribute("mensaje",
                    "Existencia de " + p.getNombre() + " actualizada a " + p.getExistenciaTexto() + ".");
            flash.addFlashAttribute("tipoMensaje", "success");
        }, () -> {
            flash.addFlashAttribute("mensaje", "Producto no encontrado.");
            flash.addFlashAttribute("tipoMensaje", "error");
        });
        return "redirect:/admin/inventario";
    }

    private boolean esNegativo(BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) < 0;
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
