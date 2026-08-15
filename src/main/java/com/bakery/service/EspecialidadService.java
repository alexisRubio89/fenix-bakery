package com.bakery.service;

import com.bakery.model.Especialidad;
import com.bakery.model.EspecialidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EspecialidadService {

    public static final int TOTAL = 4;

    @Autowired private EspecialidadRepository repo;
    @Autowired private MenuService menuService;
    @Autowired private CakeService cakeService;

    /** Datos ya resueltos para pintar una tarjeta en la portada. */
    public static class Tarjeta {
        private final String nombre, nombreEn, descripcion, descripcionEn, imagen, url;

        public Tarjeta(String nombre, String nombreEn, String descripcion,
                       String descripcionEn, String imagen, String url) {
            this.nombre = nombre;
            this.nombreEn = nombreEn;
            this.descripcion = descripcion;
            this.descripcionEn = descripcionEn;
            this.imagen = imagen;
            this.url = url;
        }

        public String getNombre() { return nombre; }
        public String getNombreEn() { return nombreEn; }
        public String getDescripcion() { return descripcion; }
        public String getDescripcionEn() { return descripcionEn; }
        public String getImagen() { return imagen; }
        public String getUrl() { return url; }
    }

    /**
     * Devuelve las tarjetas listas para la portada.
     * Si una referencia apunta a un producto que ya no existe, se omite en vez de
     * generar un enlace roto.
     */
    public List<Tarjeta> obtenerTarjetas() {
        List<Tarjeta> tarjetas = new ArrayList<>();
        for (Especialidad e : repo.findAllByOrderByPosicionAsc()) {
            resolver(e).ifPresent(tarjetas::add);
        }
        return tarjetas;
    }

    private static final String PLACEHOLDER = "/img/menu-placeholder.jpg";

    private Optional<Tarjeta> resolver(Especialidad e) {
        if (e.getSlug() == null || e.getSlug().isBlank()) return Optional.empty();

        if (e.esCake()) {
            return cakeService.obtenerPorSlug(e.getSlug()).map(c -> new Tarjeta(
                    c.getNombre(), c.getNombreEn(),
                    c.getDescripcion(), c.getDescripcionEn(),
                    imagenOPlaceholder(c.getImagen()), "/cakes/" + c.getSlug()));
        }
        return menuService.obtenerPorSlug(e.getSlug()).map(p -> new Tarjeta(
                p.getNombre(), p.getNombreEn(),
                p.getDescripcion(), p.getDescripcionEn(),
                imagenOPlaceholder(p.getImagen()), "/menu/" + p.getSlug()));
    }

    /**
     * Si el producto no tiene foto, devuelve el placeholder. Sin esto Thymeleaf
     * omite el atributo src y el onerror de la plantilla nunca llega a dispararse.
     */
    private String imagenOPlaceholder(String imagen) {
        if (imagen == null || imagen.isBlank()) return PLACEHOLDER;
        String primera = imagen.split(",")[0].trim();
        return primera.isEmpty() ? PLACEHOLDER : primera;
    }

    // ── Admin ──────────────────────────────────────

    /** Las 4 filas, creando las que falten para que el formulario siempre tenga 4. */
    public List<Especialidad> obtenerParaEditar() {
        List<Especialidad> lista = new ArrayList<>();
        for (int i = 1; i <= TOTAL; i++) {
            final int pos = i;
            lista.add(repo.findByPosicion(pos)
                    .orElseGet(() -> new Especialidad(pos, "MENU", "")));
        }
        return lista;
    }

    /**
     * Guarda las 4 posiciones. Cada valor llega como "MENU:slug" o "CAKE:slug"
     * (o vacío para dejar el hueco sin asignar).
     */
    public void guardar(List<String> valores) {
        for (int i = 0; i < TOTAL; i++) {
            int pos = i + 1;
            String valor = (i < valores.size() && valores.get(i) != null)
                    ? valores.get(i).trim() : "";

            Especialidad e = repo.findByPosicion(pos)
                    .orElseGet(() -> new Especialidad(pos, "MENU", ""));

            if (valor.isEmpty()) {
                e.setTipo("MENU");
                e.setSlug("");
            } else {
                int sep = valor.indexOf(':');
                if (sep > 0) {
                    e.setTipo(valor.substring(0, sep));
                    e.setSlug(valor.substring(sep + 1));
                }
            }
            repo.save(e);
        }
    }
}
