package com.bakery.model;

import jakarta.persistence.*;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String nombre;
    private String nombreEn;

    @Column(nullable = false)
    private String categoria;
    private String categoriaEn;

    @Column(length = 600)
    private String descripcion;
    @Column(length = 600)
    private String descripcionEn;

    @Column(length = 600)
    private String ingredientes;
    @Column(length = 600)
    private String ingredientesEn;

    private String precio;
    private String imagen;
    private String video;
    private boolean popular = false;

    // Orden de aparición (para mantener el orden del menú)
    private Integer orden = 0;

    public MenuItem() {}

    public MenuItem(String slug, String nombre, String nombreEn, String categoria, String categoriaEn,
                    String descripcion, String descripcionEn, String ingredientes, String ingredientesEn,
                    String precio, String imagen, boolean popular, Integer orden) {
        this.slug = slug;
        this.nombre = nombre;
        this.nombreEn = nombreEn;
        this.categoria = categoria;
        this.categoriaEn = categoriaEn;
        this.descripcion = descripcion;
        this.descripcionEn = descripcionEn;
        this.ingredientes = ingredientes;
        this.ingredientesEn = ingredientesEn;
        this.precio = precio;
        this.imagen = imagen;
        this.popular = popular;
        this.orden = orden;
    }

    // Genera el slug de categoría (igual que el modelo viejo, para los filtros)
    public String getCatSlug() {
        return categoria.toLowerCase()
            .replace("é","e").replace("è","e")
            .replace("á","a").replace("à","a")
            .replace("í","i").replace("ì","i")
            .replace("ó","o").replace("ò","o")
            .replace("ú","u").replace("ù","u")
            .replace("ñ","n").replace(" ","-");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNombreEn() { return nombreEn; }
    public void setNombreEn(String nombreEn) { this.nombreEn = nombreEn; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getCategoriaEn() { return categoriaEn; }
    public void setCategoriaEn(String categoriaEn) { this.categoriaEn = categoriaEn; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getDescripcionEn() { return descripcionEn; }
    public void setDescripcionEn(String descripcionEn) { this.descripcionEn = descripcionEn; }
    public String getIngredientes() { return ingredientes; }
    public void setIngredientes(String ingredientes) { this.ingredientes = ingredientes; }
    public String getIngredientesEn() { return ingredientesEn; }
    public void setIngredientesEn(String ingredientesEn) { this.ingredientesEn = ingredientesEn; }
    public String getPrecio() { return precio; }
    public void setPrecio(String precio) { this.precio = precio; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public String getVideo() { return video; }
    public void setVideo(String video) { this.video = video; }
    public boolean isTieneVideo() { return video != null && !video.isBlank(); }
    public boolean isPopular() { return popular; }
    public void setPopular(boolean popular) { this.popular = popular; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
