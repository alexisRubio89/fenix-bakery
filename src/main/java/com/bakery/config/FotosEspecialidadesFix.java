package com.bakery.config;

import com.bakery.model.Cake;
import com.bakery.model.CakeRepository;
import com.bakery.model.MenuItem;
import com.bakery.model.MenuItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Asegura que los productos mostrados en "Nuestras especialidades" tengan una foto
 * que exista de verdad, para que la portada no se vea rota desde el primer arranque.
 *
 * Solo actúa cuando la foto actual del producto apunta a un archivo local inexistente.
 * Si la foto está en S3 (subida desde el admin) o el archivo local sí existe,
 * no toca nada. Es decir: puedes cambiar la foto cuando quieras y esto la respeta.
 */
@Component
@Order(101)
public class FotosEspecialidadesFix implements CommandLineRunner {

    private final MenuItemRepository menuRepo;
    private final CakeRepository cakeRepo;

    public FotosEspecialidadesFix(MenuItemRepository menuRepo, CakeRepository cakeRepo) {
        this.menuRepo = menuRepo;
        this.cakeRepo = cakeRepo;
    }

    /** slug del producto → foto conocida que sí existe en static/img. */
    private static final Map<String, String> FOTOS_MENU = new LinkedHashMap<>() {{
        put("pan-cubano",   "/img/menu-pachy.jpg");
        put("cuban-flan",   "/img/menu-flan.jpg");
        put("pastel-guava", "/img/menu-pastelito-guayaba.jpg");
    }};

    private static final Map<String, String> FOTOS_CAKE = new LinkedHashMap<>() {{
        put("cake-tres-leches", "/img/tres-leches1.jpg");
    }};

    @Override
    public void run(String... args) {
        FOTOS_MENU.forEach((slug, foto) -> {
            Optional<MenuItem> opt = menuRepo.findBySlug(slug);
            if (opt.isEmpty()) return;
            MenuItem item = opt.get();
            if (necesitaArreglo(item.getImagen())) {
                item.setImagen(foto);
                menuRepo.save(item);
            }
        });

        FOTOS_CAKE.forEach((slug, foto) -> {
            Optional<Cake> opt = cakeRepo.findBySlug(slug);
            if (opt.isEmpty()) return;
            Cake cake = opt.get();
            if (necesitaArreglo(cake.getImagen())) {
                cake.setImagenes(foto);
                cakeRepo.save(cake);
            }
        });
    }

    /**
     * true si conviene poner una foto real: cuando la ruta está vacía, apunta a un
     * archivo local inexistente, o es uno de los placeholders genéricos del seeder.
     * Las URLs de S3 y las fotos locales reales se dejan intactas.
     */
    private boolean necesitaArreglo(String ruta) {
        if (ruta == null || ruta.isBlank()) return true;
        if (!ruta.startsWith("/img/")) return false;      // S3 u otra fuente: no tocar

        String primera = ruta.split(",")[0].trim();       // Cake guarda varias separadas por coma
        if (primera.contains("placeholder")) return true; // foto genérica: sustituir
        return !new ClassPathResource("static" + primera).exists();
    }
}
