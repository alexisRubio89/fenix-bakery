package com.bakery.config;

import com.bakery.model.Especialidad;
import com.bakery.model.EspecialidadRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Carga inicial de las 4 especialidades de la portada.
 * Se ejecuta después de los seeders de menú y cakes para que los slugs ya existan.
 */
@Component
@Order(100)
public class EspecialidadSeeder implements CommandLineRunner {

    private final EspecialidadRepository repo;

    public EspecialidadSeeder(EspecialidadRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;

        repo.save(new Especialidad(1, "MENU", "pan-cubano"));
        repo.save(new Especialidad(2, "MENU", "cuban-flan"));
        repo.save(new Especialidad(3, "MENU", "pastel-guava"));
        repo.save(new Especialidad(4, "CAKE", "cake-tres-leches"));
    }
}
