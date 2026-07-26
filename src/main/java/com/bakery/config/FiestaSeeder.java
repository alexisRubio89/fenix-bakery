package com.bakery.config;

import com.bakery.model.Fiesta;
import com.bakery.model.FiestaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class FiestaSeeder implements CommandLineRunner {

    private final FiestaRepository repo;

    public FiestaSeeder(FiestaRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) {
            return;
        }

        List<Fiesta> productos = List.of(
            new Fiesta("pasteles-surtidos", "Bandeja de Pasteles Surtidos", "Assorted Pastry Tray",
                "Selección de pasteles cubanos variados, ideal para compartir en cualquier celebración.",
                "Selection of assorted Cuban pastries, ideal for sharing at any celebration.",
                "Bandeja con surtido de pastelitos de guayaba, carne, queso y coco. Disponible en distintos tamaños según el número de invitados.",
                "Tray with an assortment of guava, meat, cheese and coconut pastries. Available in different sizes based on guest count.",
                "/img/fiesta-placeholder-1.jpg", true, 1),

            new Fiesta("ensalada-fria", "Ensalada Fría", "Cold Salad",
                "Clásica ensalada fría cubana, cremosa y refrescante. Un imprescindible en toda fiesta.",
                "Classic Cuban cold salad, creamy and refreshing. A must at every party.",
                "Ensalada fría tradicional con coditos, jamón, queso, vegetales y mayonesa. Preparada fresca para tu evento. Disponible por libras o bandeja completa.",
                "Traditional cold salad with elbow pasta, ham, cheese, vegetables and mayonnaise. Freshly prepared for your event. Available by the pound or full tray.",
                "/img/fiesta-placeholder-2.jpg", true, 2),

            new Fiesta("bocaditos", "Bocaditos", "Finger Sandwiches",
                "Pan suave relleno de pasta cubana. Pequeños, sabrosos y perfectos para picar.",
                "Soft bread filled with Cuban spread. Small, tasty and perfect for snacking.",
                "Bocaditos de pan suave rellenos de pasta de jamón, pollo o pescado. Se venden por docena o en bandejas para fiestas grandes.",
                "Soft bread finger sandwiches filled with ham, chicken or fish spread. Sold by the dozen or in trays for large parties.",
                "/img/fiesta-placeholder-3.jpg", true, 3),

            new Fiesta("croquetas-fiesta", "Croquetas para Fiesta", "Party Croquettes",
                "Croquetas crujientes por fuera y cremosas por dentro. Por docenas para tu evento.",
                "Croquettes crispy outside and creamy inside. By the dozen for your event.",
                "Croquetas de jamón o pollo, fritas al momento. Disponibles por docena o bandejas para eventos.",
                "Ham or chicken croquettes, freshly fried. Available by the dozen or in trays for events.",
                "/img/fiesta-placeholder-1.jpg", false, 4),

            new Fiesta("empanadas-fiesta", "Empanadas para Fiesta", "Party Empanadas",
                "Empanadas rellenas de carne sazonada al estilo cubano. Ideales para grupos.",
                "Empanadas filled with Cuban-style seasoned meat. Ideal for groups.",
                "Empanadas fritas rellenas de carne, pollo o queso. Se preparan por docena o bandeja para fiestas.",
                "Fried empanadas filled with meat, chicken or cheese. Prepared by the dozen or tray for parties.",
                "/img/fiesta-placeholder-2.jpg", false, 5)
        );

        repo.saveAll(productos);
        System.out.println("[FiestaSeeder] " + productos.size() + " productos de fiesta cargados a la base de datos.");
    }
}
