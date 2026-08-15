package com.bakery.config;

import com.bakery.model.Producto;
import com.bakery.model.ProductoRepository;
import com.bakery.model.TipoProducto;
import com.bakery.model.UnidadMedida;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Carga inicial del almacén con los productos reales de la panadería,
 * tomados de las hojas "Productos Almacén" y "Solicitud de Productos"
 * (proveedores Dawn y Oliverys).
 *
 * Las existencias entran en 0 y sin precio a propósito: el conteo real y los
 * costos se cargan desde /admin/inventario. Así el valor del almacén nunca
 * muestra una cifra inventada.
 *
 * Las "unidades por caja" también quedan vacías porque ese dato depende del
 * formato de compra de cada proveedor. Al llenarlo en cada producto, la lista
 * empieza a mostrar el desglose "2 cajas + 37 sueltas".
 */
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner cargarDatos(ProductoRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            cargarInsumos(repo);
            cargarIngredientesDawn(repo);
            cargarIngredientesOliverys(repo);
            cargarIngredientesGenerales(repo);
        };
    }

    // ══ INSUMOS (hoja "Productos Almacén") ═══════════════════════
    private void cargarInsumos(ProductoRepository repo) {
        insumo(repo, "Cartucho #4", "Cartuchos y bolsas", UnidadMedida.CAJA);
        insumo(repo, "Cartucho #6", "Cartuchos y bolsas", UnidadMedida.CAJA);
        insumo(repo, "Cartucho #10", "Cartuchos y bolsas", UnidadMedida.CAJA);
        insumo(repo, "Shopping (Bolsas)", "Cartuchos y bolsas", UnidadMedida.PAQUETE);

        insumo(repo, "Papel Blanco para Sándwich", "Papel y envolturas", UnidadMedida.CAJA);
        insumo(repo, "Papel Aluminio para Sándwich", "Papel y envolturas", UnidadMedida.CAJA);
        insumo(repo, "Papel para agarrar dulces", "Papel y envolturas", UnidadMedida.CAJA);

        insumo(repo, "Removedores Grandes", "Servilletas y removedores", UnidadMedida.CAJA);
        insumo(repo, "Removedores Pequeños", "Servilletas y removedores", UnidadMedida.CAJA);
        insumo(repo, "Straws", "Servilletas y removedores", UnidadMedida.CAJA);
        insumo(repo, "Servilletas", "Servilletas y removedores", UnidadMedida.CAJA);

        insumo(repo, "Guantes Vinyl", "Higiene y protección", UnidadMedida.CAJA);
        insumo(repo, "Guantes Poly", "Higiene y protección", UnidadMedida.CAJA);

        insumo(repo, "Baking Cup 350", "Moldes y capacillos", UnidadMedida.CAJA);
        insumo(repo, "Baking Cup 450", "Moldes y capacillos", UnidadMedida.CAJA);

        insumo(repo, "Platos Pequeños", "Platos y bandejas", UnidadMedida.CAJA);
        insumo(repo, "Platos Grandes", "Platos y bandejas", UnidadMedida.CAJA);

        insumo(repo, "Cajita Biodegradable", "Cajas y empaques", UnidadMedida.CAJA);
        insumo(repo, "Cajita Transparente", "Cajas y empaques", UnidadMedida.CAJA);
        insumo(repo, "Portavasos Individual", "Cajas y empaques", UnidadMedida.CAJA);
        insumo(repo, "Portavasos de 4 (cuadrado)", "Cajas y empaques", UnidadMedida.CAJA);

        insumo(repo, "Vasos Dependes", "Vasos y tapas", UnidadMedida.CAJA);
        insumo(repo, "Tapas Dependes", "Vasos y tapas", UnidadMedida.CAJA);
        insumo(repo, "Vaso 4 oz", "Vasos y tapas", UnidadMedida.CAJA);
        insumo(repo, "Tapa 4 oz", "Vasos y tapas", UnidadMedida.CAJA);
        insumo(repo, "Vaso 8 oz", "Vasos y tapas", UnidadMedida.CAJA);
        insumo(repo, "Tapa 8 oz", "Vasos y tapas", UnidadMedida.CAJA);
        insumo(repo, "Vaso 12 oz", "Vasos y tapas", UnidadMedida.CAJA);
        insumo(repo, "Vaso 16 oz", "Vasos y tapas", UnidadMedida.CAJA);
        insumo(repo, "Tapa 12-16 oz", "Vasos y tapas", UnidadMedida.CAJA);
        insumo(repo, "Vaso Batido", "Vasos y tapas", UnidadMedida.CAJA);
        insumo(repo, "Tapa Batido", "Vasos y tapas", UnidadMedida.CAJA);
        insumo(repo, "Tapa Capuchino", "Vasos y tapas", UnidadMedida.CAJA);

        insumo(repo, "Cloro", "Limpieza", UnidadMedida.GALON);
        insumo(repo, "Jabón", "Limpieza", UnidadMedida.GALON);
        insumo(repo, "Líquido Plancha", "Limpieza", UnidadMedida.GALON);

        // Venía en la hoja de Dawn, pero es papel de bandeja: va con los insumos.
        insumoProv(repo, "Coater Liner 35#", "Papel y envolturas", UnidadMedida.CAJA, "Dawn", null);
    }

    // ══ INGREDIENTES · Proveedor DAWN ════════════════════════════
    private void cargarIngredientesDawn(ProductoRepository repo) {
        final String P = "Dawn";
        ing(repo, "All Trumps", "Harinas y granos", UnidadMedida.SACO, P, "219139");
        ing(repo, "Fine Yellow Corn Meal #425", "Harinas y granos", UnidadMedida.SACO, P, "3041758");
        ing(repo, "Argo Corn Starch (Cajitas)", "Harinas y granos", UnidadMedida.CAJA, P, "219519");

        ing(repo, "Natural Seedless Raisins", "Rellenos y frutas", UnidadMedida.CAJA, P, "2503573");
        ing(repo, "Bavarian Creme Fillings", "Rellenos y frutas", UnidadMedida.CAJA, P, "2405191");
        ing(repo, "Filing Coconut Creme Pie", "Rellenos y frutas", UnidadMedida.CAJA, P, "2406074");
        ing(repo, "Fillings Pineapple", "Rellenos y frutas", UnidadMedida.CAJA, P, "2407577");

        ing(repo, "BBS", "Levaduras y mejorantes", UnidadMedida.SACO, P, "2409432");
        ing(repo, "Baking Powder", "Levaduras y mejorantes", UnidadMedida.CAJA, P, "3027714");
        ing(repo, "Yeast", "Levaduras y mejorantes", UnidadMedida.CAJA, P, "917958");

        ing(repo, "Choc-Dip", "Chocolate y cacao", UnidadMedida.CAJA, P, "2478354");

        ing(repo, "Clear Pan 200", "Grasas y aceites", UnidadMedida.CAJA, P, "216572");

        ing(repo, "Flat Icing (Cobertura Para Donas) White", "Coberturas y glaseados", UnidadMedida.CAJA, P, "18804");
        ing(repo, "Fruity Pastry Glaze Strawberry Flavored", "Coberturas y glaseados", UnidadMedida.CAJA, P, "2406131");

        ing(repo, "Frozen Egg HY-TEX", "Lácteos y huevos", UnidadMedida.CAJA, P, "1364645");
        ing(repo, "Frozen Sugared Egg Yolks", "Lácteos y huevos", UnidadMedida.CAJA, P, "3072493");

        ing(repo, "Whip Deelite Base", "Cremas y batidos", UnidadMedida.CAJA, P, "1279290");
        ing(repo, "Velvetop Chocolate (Malo)", "Cremas y batidos", UnidadMedida.CAJA, P, "492695");
        ing(repo, "BetterCreme Chocolate (Bueno)", "Cremas y batidos", UnidadMedida.CAJA, P, "3031322");
        ing(repo, "Velvetop Vanilla", "Cremas y batidos", UnidadMedida.CAJA, P, "492687");

        ing(repo, "RTB Butter Spinach Feta", "Masas congeladas", UnidadMedida.CAJA, P, "1322536");
        ing(repo, "Butter Croissant", "Masas congeladas", UnidadMedida.CAJA, P, "620246");

        ing(repo, "Yellow Egg Shade Liquid Color", "Saborizantes y colorantes", UnidadMedida.UNIDAD, P, "3039373");
        ing(repo, "Flavor Vanilla Essentials", "Saborizantes y colorantes", UnidadMedida.UNIDAD, P, "3037638");

        // "Salt" aparece en las dos hojas: se registra una sola vez.
        ing(repo, "Salt", "Sal y condimentos", UnidadMedida.SACO, P, null);
    }

    // ══ INGREDIENTES · Proveedor OLIVERYS ════════════════════════
    private void cargarIngredientesOliverys(ProductoRepository repo) {
        final String P = "Oliverys";
        ing(repo, "Glutamax", "Levaduras y mejorantes", UnidadMedida.SACO, P, null);
        ing(repo, "Puratos S-500 Red", "Levaduras y mejorantes", UnidadMedida.SACO, P, null);
        ing(repo, "Instant Clear Jel", "Levaduras y mejorantes", UnidadMedida.SACO, P, null);
        ing(repo, "Sof-Tex", "Levaduras y mejorantes", UnidadMedida.SACO, P, null);

        ing(repo, "Pura Snow", "Azúcares y endulzantes", UnidadMedida.SACO, P, null);
        ing(repo, "Sugar", "Azúcares y endulzantes", UnidadMedida.SACO, P, null);

        ing(repo, "Chocolate Creme Cake Base 39270", "Bases y premezclas", UnidadMedida.SACO, P, "39270");
        ing(repo, "Creme Cake Base 39208", "Bases y premezclas", UnidadMedida.SACO, P, "39208");

        ing(repo, "Arequipe", "Rellenos y frutas", UnidadMedida.CAJA, P, null);
        ing(repo, "Strawberry Filling", "Rellenos y frutas", UnidadMedida.CAJA, P, null);
        ing(repo, "Maraschino Cherries", "Rellenos y frutas", UnidadMedida.CAJA, P, null);
        ing(repo, "Puratos Topfil Choice Guava", "Rellenos y frutas", UnidadMedida.CAJA, P, null);

        ing(repo, "Puratos Miroir Neutre Kosher", "Coberturas y glaseados", UnidadMedida.CAJA, P, null);
        ing(repo, "Hon-ee Glaze", "Coberturas y glaseados", UnidadMedida.CAJA, P, null);
        ing(repo, "Strawberry Glaze Fresa Vidriado", "Coberturas y glaseados", UnidadMedida.CAJA, P, null);

        ing(repo, "Lard", "Grasas y aceites", UnidadMedida.CAJA, P, null);

        ing(repo, "Cream Cheese", "Lácteos y huevos", UnidadMedida.CAJA, P, null);

        ing(repo, "Natural Anise Oil", "Saborizantes y colorantes", UnidadMedida.UNIDAD, P, null);
        ing(repo, "Coconut Flavor Emulsion Art", "Saborizantes y colorantes", UnidadMedida.UNIDAD, P, "6400800308");
    }

    // ══ INGREDIENTES · compra general ════════════════════════════
    private void cargarIngredientesGenerales(ProductoRepository repo) {
        ing(repo, "Huevos", "Lácteos y huevos", UnidadMedida.CAJA, null, null);
        ing(repo, "Mantequilla", "Grasas y aceites", UnidadMedida.CAJA, null, null);
        ing(repo, "Aceite", "Grasas y aceites", UnidadMedida.GALON, null, null);
    }

    // ── Helpers de alta ──────────────────────────────────────────
    private void ing(ProductoRepository repo, String nombre, String categoria,
                     UnidadMedida unidad, String proveedor, String codigo) {
        repo.save(new Producto(nombre, TipoProducto.INGREDIENTE, categoria, unidad, proveedor, codigo));
    }

    private void insumo(ProductoRepository repo, String nombre, String categoria, UnidadMedida unidad) {
        insumoProv(repo, nombre, categoria, unidad, null, null);
    }

    private void insumoProv(ProductoRepository repo, String nombre, String categoria,
                            UnidadMedida unidad, String proveedor, String codigo) {
        repo.save(new Producto(nombre, TipoProducto.INSUMO, categoria, unidad, proveedor, codigo));
    }
}
