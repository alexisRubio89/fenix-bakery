package com.bakery.service;

import com.bakery.model.Fiesta;
import com.bakery.model.FiestaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FiestaService {

    @Autowired
    private FiestaRepository repo;

    public List<Fiesta> obtenerTodos() {
        return repo.findAllByOrderByOrdenAsc();
    }

    public Optional<Fiesta> obtenerPorSlug(String slug) {
        return repo.findBySlug(slug);
    }

    public Fiesta guardar(Fiesta fiesta) {
        return repo.save(fiesta);
    }

    public Optional<Fiesta> obtenerPorId(Long id) {
        return repo.findById(id);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public long contar() {
        return repo.count();
    }
}
