package com.bakery.config;

import com.bakery.model.Producto;
import com.bakery.model.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner cargarDatos(ProductoRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                // Ingredientes
                repo.save(new Producto("Harina de trigo",       "Ingredientes", 50, 1.20, "Harina todo uso, bolsa 5lb"));
                repo.save(new Producto("Azúcar blanca",         "Ingredientes", 40, 0.90, "Azúcar refinada, bolsa 5lb"));
                repo.save(new Producto("Mantequilla sin sal",   "Ingredientes", 30, 3.50, "Bloque 1lb"));
                repo.save(new Producto("Leche condensada",      "Ingredientes", 25, 2.20, "Lata 14oz — para tres leches y natilla"));
                repo.save(new Producto("Leche evaporada",       "Ingredientes", 20, 1.80, "Lata 12oz"));
                repo.save(new Producto("Huevos (caja 12)",      "Ingredientes", 18, 3.00, "Huevos frescos"));
                repo.save(new Producto("Levadura seca",         "Ingredientes",  6, 2.00, "Levadura instantánea 100g"));
                repo.save(new Producto("Pasta de guayaba",      "Ingredientes", 15, 2.50, "Barra 1lb — para pañuelos y cakes"));
                repo.save(new Producto("Queso crema",           "Ingredientes", 12, 2.80, "Bloque 8oz"));
                repo.save(new Producto("Vainilla",              "Ingredientes", 10, 3.20, "Extracto puro, frasco 4oz"));
                // Panadería
                repo.save(new Producto("Pan Cubano (unidad)",   "Panadería",    60, 1.50, "Pan largo tradicional"));
                repo.save(new Producto("Pan de Medianoche",     "Panadería",    40, 1.25, "Pan dulce suave"));
                repo.save(new Producto("Pañuelos de guayaba",   "Panadería",    30, 2.25, "Hojaldre con guayaba y queso"));
                repo.save(new Producto("Cañas de crema",        "Panadería",    25, 2.50, "Hojaldre relleno de crema"));
                // Dulcería
                repo.save(new Producto("Flan de leche",         "Dulcería",     20, 3.50, "Porción individual"));
                repo.save(new Producto("Natilla de vainilla",   "Dulcería",     15, 2.50, "Vaso 8oz"));
                repo.save(new Producto("Arroz con leche",       "Dulcería",     12, 2.75, "Vaso 8oz con canela"));
                repo.save(new Producto("Merengues surtidos",    "Dulcería",      8, 1.50, "Docena"));
                // Cakes
                repo.save(new Producto("Cake Tres Leches",      "Cakes",        10, 4.50, "Porción individual"));
                repo.save(new Producto("Cake de Guayaba",       "Cakes",         8, 4.00, "Porción individual"));
                repo.save(new Producto("Pionono",               "Cakes",         5, 3.75, "Brazo gitano con dulce de leche"));
                // Empaques
                repo.save(new Producto("Cajas para cake",       "Empaques",     80, 0.50, "Caja blanca 10x10"));
                repo.save(new Producto("Bolsas de papel kraft", "Empaques",    150, 0.20, "Para pan cubano"));
            }
        };
    }
}
