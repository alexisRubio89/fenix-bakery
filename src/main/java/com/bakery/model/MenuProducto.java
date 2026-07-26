package com.bakery.model;

public class MenuProducto {
    private String slug;
    private String nombre;
    private String nombreEn;
    private String categoria;
    private String categoriaEn;
    private String descripcion;
    private String descripcionEn;
    private String ingredientes;
    private String ingredientesEn;
    private String precio;
    private String imagen;
    private boolean popular;

    public MenuProducto(String slug, String nombre, String nombreEn, String categoria, String categoriaEn,
                        String descripcion, String descripcionEn, String ingredientes, String ingredientesEn,
                        String precio, String imagen, boolean popular) {
        this.slug = slug; this.nombre = nombre; this.nombreEn = nombreEn;
        this.categoria = categoria; this.categoriaEn = categoriaEn;
        this.descripcion = descripcion; this.descripcionEn = descripcionEn;
        this.ingredientes = ingredientes; this.ingredientesEn = ingredientesEn;
        this.precio = precio; this.imagen = imagen; this.popular = popular;
    }

    public String getSlug() { return slug; }
    public String getNombre() { return nombre; }
    public String getNombreEn() { return nombreEn; }
    public String getCategoria() { return categoria; }
    public String getCategoriaEn() { return categoriaEn; }
    public String getDescripcion() { return descripcion; }
    public String getDescripcionEn() { return descripcionEn; }
    public String getIngredientes() { return ingredientes; }
    public String getIngredientesEn() { return ingredientesEn; }
    public String getPrecio() { return precio; }
    public String getImagen() { return imagen; }
    public boolean isPopular() { return popular; }
    public String getCatSlug() {
        return categoria.toLowerCase()
            .replace("é","e").replace("è","e")
            .replace("á","a").replace("à","a")
            .replace("í","i").replace("ì","i")
            .replace("ó","o").replace("ò","o")
            .replace("ú","u").replace("ù","u")
            .replace("ñ","n").replace(" ","-");
    }
}
