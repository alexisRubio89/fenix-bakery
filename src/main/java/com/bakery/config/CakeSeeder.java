package com.bakery.config;

import com.bakery.model.Cake;
import com.bakery.model.CakeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CakeSeeder implements CommandLineRunner {

    private final CakeRepository repo;

    public CakeSeeder(CakeRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) {
            return;
        }

        List<Cake> cakes = List.of(
            new Cake("mini-cake-fresa", "Mini Cake de Fresa", "Strawberry Mini Cake",
                "Mini Cakes", "Mini Cakes",
                "Pequeño bizcocho esponjoso con crema y fresas frescas. Porción individual perfecta.",
                "Small fluffy sponge cake with cream and fresh strawberries. Perfect individual portion.",
                List.of("/img/cake-placeholder-1.jpg", "/img/cake-placeholder-2.jpg", "/img/cake-placeholder-3.jpg"),
                true, 1),

            new Cake("mini-cake-chocolate", "Mini Cake de Chocolate", "Chocolate Mini Cake",
                "Mini Cakes", "Mini Cakes",
                "Bizcocho de chocolate con ganache y decoración elegante. Tamaño individual.",
                "Chocolate sponge with ganache and elegant decoration. Individual size.",
                List.of("/img/cake-placeholder-1.jpg", "/img/cake-placeholder-2.jpg"),
                false, 2),

            new Cake("mini-cake-vainilla", "Mini Cake de Vainilla", "Vanilla Mini Cake",
                "Mini Cakes", "Mini Cakes",
                "Clásico bizcocho de vainilla con buttercream y detalles dorados.",
                "Classic vanilla sponge with buttercream and golden details.",
                List.of("/img/cake-placeholder-1.jpg", "/img/cake-placeholder-3.jpg"),
                false, 3),

            new Cake("cake-tres-leches", "Cake Tres Leches", "Tres Leches Cake",
                "Cakes de la Casa", "House Cakes",
                "Esponjoso, húmedo, bañado en tres leches. El postre favorito de toda celebración cubana.",
                "Fluffy, moist, soaked in three milks. The favorite dessert of every Cuban celebration.",
                List.of("/img/cake-placeholder-1.jpg", "/img/cake-placeholder-2.jpg", "/img/cake-placeholder-3.jpg"),
                true, 4),

            new Cake("cake-flores", "Cake de Flores", "Floral Cake",
                "Cakes de la Casa", "House Cakes",
                "Decorado con flores de azúcar hechas a mano. Una obra de arte comestible.",
                "Decorated with handmade sugar flowers. An edible work of art.",
                List.of("/img/cake-flores-1.jpeg", "/img/cake-flores-2.jpeg", "/img/cake-flores-3.jpeg",
                        "/img/cake-flores-4.jpeg", "/img/cake-flores-5.jpeg"),
                true, 5),

            new Cake("cake-tematico", "Cake Temático", "Themed Cake",
                "Cakes Personalizados", "Custom Cakes",
                "Cualquier temática que imagines: personajes, hobbies, profesiones. Tú lo sueñas, lo creamos.",
                "Any theme you imagine: characters, hobbies, professions. You dream it, we create it.",
                List.of("/img/cake-tematico-1.jpeg", "/img/cake-tematico-2.jpeg", "/img/cake-tematico-3.jpeg",
                        "/img/cake-tematico-4.jpeg", "/img/cake-tematico-5.jpeg", "/img/cake-tematico-6.jpeg",
                        "/img/cake-tematico-7.jpeg", "/img/cake-tematico-8.jpeg", "/img/cake-tematico-9.jpeg",
                        "/img/cake-tematico-10.jpeg", "/img/cake-tematico-11.jpeg", "/img/cake-tematico-12.jpeg",
                        "/img/cake-tematico-13.jpeg", "/img/cake-tematico-14.jpeg"),
                false, 6)
        );

        repo.saveAll(cakes);
        System.out.println("[CakeSeeder] " + cakes.size() + " cakes cargados a la base de datos.");
    }
}
