package com.bakery.service;

import com.bakery.model.MenuItem;
import com.bakery.model.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MenuService {

    @Autowired
    private MenuItemRepository repo;

    public List<MenuItem> obtenerTodos() {
        return repo.findAllByOrderByOrdenAsc();
    }

    public Optional<MenuItem> obtenerPorSlug(String slug) {
        return repo.findBySlug(slug);
    }

    public List<MenuItem> obtenerPorCategoria(String catSlug) {
        return repo.findAllByOrderByOrdenAsc().stream()
                .filter(p -> p.getCatSlug().equals(catSlug))
                .toList();
    }

    // ── Métodos para el admin (CRUD) ──────────────
    public MenuItem guardar(MenuItem item) {
        return repo.save(item);
    }

    public Optional<MenuItem> obtenerPorId(Long id) {
        return repo.findById(id);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public long contar() {
        return repo.count();
    }
}
