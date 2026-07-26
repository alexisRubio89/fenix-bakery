package com.bakery.service;

import com.bakery.model.Cake;
import com.bakery.model.CakeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CakeService {

    @Autowired
    private CakeRepository repo;

    public List<Cake> obtenerTodos() {
        return repo.findAllByOrderByOrdenAsc();
    }

    public Optional<Cake> obtenerPorSlug(String slug) {
        return repo.findBySlug(slug);
    }

    public List<Cake> obtenerPorCategoria(String catSlug) {
        return repo.findAllByOrderByOrdenAsc().stream()
                .filter(c -> c.getCatSlug().equals(catSlug))
                .toList();
    }

    public Cake guardar(Cake cake) {
        return repo.save(cake);
    }

    public Optional<Cake> obtenerPorId(Long id) {
        return repo.findById(id);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public long contar() {
        return repo.count();
    }
}
