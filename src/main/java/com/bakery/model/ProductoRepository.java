package com.bakery.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // ── Filtros por tipo (ingredientes / insumos) ──
    List<Producto> findByTipoOrderByNombreAsc(TipoProducto tipo);

    List<Producto> findByTipoAndCategoriaOrderByNombreAsc(TipoProducto tipo, String categoria);

    long countByTipo(TipoProducto tipo);

    // ── Filtros por categoría ──────────────────────
    List<Producto> findByCategoriaOrderByNombreAsc(String categoria);

    // ── Búsqueda ───────────────────────────────────
    List<Producto> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

    List<Producto> findByTipoAndNombreContainingIgnoreCaseOrderByNombreAsc(TipoProducto tipo, String nombre);

    /**
     * Stock bajo: se compara contra el mínimo propio de cada producto,
     * no contra un número fijo. Los agotados salen primero.
     */
    @Query("SELECT p FROM Producto p WHERE p.cantidad <= p.stockMinimo ORDER BY p.cantidad ASC, p.nombre ASC")
    List<Producto> findStockBajo();

    @Query("SELECT p FROM Producto p WHERE p.tipo = :tipo AND p.cantidad <= p.stockMinimo ORDER BY p.cantidad ASC, p.nombre ASC")
    List<Producto> findStockBajoPorTipo(TipoProducto tipo);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.cantidad <= p.stockMinimo")
    long contarStockBajo();

    /** Lista ordenada, para la vista sin filtros. */
    List<Producto> findAllByOrderByTipoAscNombreAsc();
}
