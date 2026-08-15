package com.bakery.model;

import jakarta.persistence.*;

/**
 * Una de las 4 tarjetas de "Nuestras especialidades" de la portada.
 * No guarda nombre/foto: solo apunta a un producto existente (del menú o de cakes),
 * de modo que los datos siempre salen del producto real y nunca quedan desfasados.
 */
@Entity
@Table(name = "especialidad")
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Posición en la portada: 1, 2, 3 o 4. */
    @Column(nullable = false, unique = true)
    private Integer posicion;

    /** "MENU" o "CAKE". */
    @Column(nullable = false, length = 10)
    private String tipo;

    /** Slug del producto referenciado. */
    @Column(nullable = false)
    private String slug;

    public Especialidad() {}

    public Especialidad(Integer posicion, String tipo, String slug) {
        this.posicion = posicion;
        this.tipo = tipo;
        this.slug = slug;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getPosicion() { return posicion; }
    public void setPosicion(Integer posicion) { this.posicion = posicion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public boolean esCake() { return "CAKE".equalsIgnoreCase(tipo); }
}
