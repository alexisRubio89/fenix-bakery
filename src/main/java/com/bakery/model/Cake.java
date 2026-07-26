package com.bakery.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "cakes")
@Access(AccessType.FIELD)
public class Cake {

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

    // Las URLs de las fotos se guardan separadas por "|"
    @Column(length = 4000)
    private String imagenes;

    private String video;

    private boolean popular = false;

    private Integer orden = 0;

    public Cake() {}

    public Cake(String slug, String nombre, String nombreEn, String categoria, String categoriaEn,
                String descripcion, String descripcionEn, List<String> imagenes, boolean popular, Integer orden) {
        this.slug = slug;
        this.nombre = nombre;
        this.nombreEn = nombreEn;
        this.categoria = categoria;
        this.categoriaEn = categoriaEn;
        this.descripcion = descripcion;
        this.descripcionEn = descripcionEn;
        setImagenesList(imagenes);
        this.popular = popular;
        this.orden = orden;
    }

    // ── Helpers para la lista de imágenes ──────────
    // getImagenes() devuelve la LISTA (las plantillas la iteran con th:each)
    @Transient
    public List<String> getImagenes() {
        if (imagenes == null || imagenes.isBlank()) return new ArrayList<>();
        List<String> lista = new ArrayList<>();
        for (String s : imagenes.split("\\|")) {
            if (!s.isBlank()) lista.add(s.trim());
        }
        return lista;
    }

    @Transient
    public List<String> getImagenesList() {
        return getImagenes();
    }

    public void setImagenesList(List<String> lista) {
        if (lista == null || lista.isEmpty()) {
            this.imagenes = "";
        } else {
            this.imagenes = String.join("|", lista);
        }
    }

    // Acceso al String crudo guardado en BD (para uso interno/JPA)
    @Transient
    public String getImagenesRaw() { return imagenes; }
    public void setImagenesRaw(String imagenes) { this.imagenes = imagenes; }

    // Primera imagen (para la tarjeta/listado)
    @Transient
    public String getImagen() {
        List<String> lista = getImagenes();
        return lista.isEmpty() ? "" : lista.get(0);
    }

    @Transient
    public String getCatSlug() {
        return categoria.toLowerCase()
            .replace("é","e").replace("è","e")
            .replace("á","a").replace("à","a")
            .replace("í","i").replace("ì","i")
            .replace("ó","o").replace("ò","o")
            .replace("ú","u").replace("ù","u")
            .replace("ñ","n").replace(" ","-");
    }

    // ── Getters/setters ────────────────────────────
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
    public void setImagenes(String imagenes) { this.imagenes = imagenes; }
    public boolean isPopular() { return popular; }
    public void setPopular(boolean popular) { this.popular = popular; }
    public String getVideo() { return video; }
    public void setVideo(String video) { this.video = video; }
    @Transient
    public boolean isTieneVideo() { return video != null && !video.isBlank(); }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
