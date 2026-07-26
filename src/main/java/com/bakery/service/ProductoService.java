package com.bakery.service;

import com.bakery.model.Producto;
import com.bakery.model.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository repo;

    // ── CRUD básico ───────────────────────────────
    public List<Producto> obtenerTodos() {
        return repo.findAll();
    }

    public Optional<Producto> obtenerPorId(long id) {
        return repo.findById(id);
    }

    // Línea 29 — evitar warning con cast explícito
    public Producto guardar(Producto p) {
        Producto guardado = repo.save(p);
        return guardado;
    }

    public void eliminar(long id) {
        repo.deleteById(id);
    }

    // ── Búsqueda ──────────────────────────────────
    public List<Producto> buscarPorNombre(String nombre) {
        return repo.findByNombreContainingIgnoreCase(nombre);
    }

    public List<Producto> buscarPorCategoria(String categoria) {
        return repo.findByCategoria(categoria);
    }

    // ── Estadísticas para el dashboard ────────────
    public long totalProductos() {
        return repo.count();
    }

    public List<Producto> stockBajo() {
        return repo.findStockBajo();
    }

    public double valorTotalInventario() {
        return repo.findAll().stream()
                .mapToDouble(p -> {
                    Double precio    = p.getPrecio();
                    Integer cantidad = p.getCantidad();
                    return (precio != null ? precio : 0.0)
                         * (cantidad != null ? cantidad : 0);
                })
                .sum();
    }

    // Línea 68 — reemplazar Long::sum por lambda explícita para evitar warning
    public Map<String, Long> productosPorCategoria() {
        Map<String, Long> mapa = new LinkedHashMap<>();
        repo.findAll().forEach(p -> {
            String cat = p.getCategoria() != null ? p.getCategoria() : "Sin categoría";
            mapa.merge(cat, 1L, (a, b) -> a + b);
        });
        return mapa;
    }
}
