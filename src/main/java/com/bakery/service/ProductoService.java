package com.bakery.service;

import com.bakery.model.Producto;
import com.bakery.model.ProductoRepository;
import com.bakery.model.TipoProducto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository repo;

    /**
     * Categorías sugeridas para cada tipo. Se guardan como texto en el producto,
     * así que agregar una opción aquí no requiere tocar la base de datos.
     */
    public static final List<String> CATEGORIAS_INGREDIENTE = List.of(
            "Harinas y granos",
            "Azúcares y endulzantes",
            "Grasas y aceites",
            "Lácteos y huevos",
            "Rellenos y frutas",
            "Coberturas y glaseados",
            "Cremas y batidos",
            "Bases y premezclas",
            "Levaduras y mejorantes",
            "Saborizantes y colorantes",
            "Chocolate y cacao",
            "Masas congeladas",
            "Sal y condimentos",
            "Otros ingredientes"
    );

    public static final List<String> CATEGORIAS_INSUMO = List.of(
            "Cartuchos y bolsas",
            "Papel y envolturas",
            "Vasos y tapas",
            "Platos y bandejas",
            "Servilletas y removedores",
            "Moldes y capacillos",
            "Cajas y empaques",
            "Higiene y protección",
            "Limpieza",
            "Papelería y etiquetas",
            "Decoración de cakes",
            "Otros insumos"
    );

    public List<String> categoriasDe(TipoProducto tipo) {
        return tipo == TipoProducto.INSUMO ? CATEGORIAS_INSUMO : CATEGORIAS_INGREDIENTE;
    }

    // ── CRUD básico ───────────────────────────────
    public List<Producto> obtenerTodos() {
        return repo.findAllByOrderByTipoAscNombreAsc();
    }

    public Optional<Producto> obtenerPorId(long id) {
        return repo.findById(id);
    }

    public Producto guardar(Producto p) {
        return repo.save(p);
    }

    public void eliminar(long id) {
        repo.deleteById(id);
    }

    // ── Filtros ───────────────────────────────────
    public List<Producto> obtenerPorTipo(TipoProducto tipo) {
        return repo.findByTipoOrderByNombreAsc(tipo);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return repo.findByNombreContainingIgnoreCaseOrderByNombreAsc(nombre);
    }

    public List<Producto> buscarPorNombre(TipoProducto tipo, String nombre) {
        if (tipo == null) return buscarPorNombre(nombre);
        return repo.findByTipoAndNombreContainingIgnoreCaseOrderByNombreAsc(tipo, nombre);
    }

    public List<Producto> buscarPorCategoria(String categoria) {
        return repo.findByCategoriaOrderByNombreAsc(categoria);
    }

    public List<Producto> buscarPorCategoria(TipoProducto tipo, String categoria) {
        if (tipo == null) return buscarPorCategoria(categoria);
        return repo.findByTipoAndCategoriaOrderByNombreAsc(tipo, categoria);
    }

    // ── Estadísticas para el dashboard ────────────
    public long totalProductos() {
        return repo.count();
    }

    public long totalPorTipo(TipoProducto tipo) {
        return repo.countByTipo(tipo);
    }

    /** Productos que llegaron a su punto de reposición. */
    public List<Producto> stockBajo() {
        return repo.findStockBajo();
    }

    public List<Producto> stockBajo(TipoProducto tipo) {
        if (tipo == null) return stockBajo();
        return repo.findStockBajoPorTipo(tipo);
    }

    public long contarStockBajo() {
        return repo.contarStockBajo();
    }

    public double valorTotalInventario() {
        return repo.findAll().stream()
                .map(Producto::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }

    public Map<String, Long> productosPorCategoria() {
        Map<String, Long> mapa = new LinkedHashMap<>();
        obtenerTodos().forEach(p -> {
            String cat = p.getCategoria() != null ? p.getCategoria() : "Sin categoría";
            mapa.merge(cat, 1L, (a, b) -> a + b);
        });
        return mapa;
    }
}
