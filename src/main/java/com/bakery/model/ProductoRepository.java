package com.bakery.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Buscar por categoría
    List<Producto> findByCategoria(String categoria);

    // Buscar por nombre (contiene, sin importar mayúsculas)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // Productos con stock bajo (menos de 10 unidades)
    @Query("SELECT p FROM Producto p WHERE p.cantidad < 10")
    List<Producto> findStockBajo();

    // Total de productos distintos
    long count();
}
