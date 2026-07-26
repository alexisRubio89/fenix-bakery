package com.bakery.model;

import jakarta.persistence.*;

@Entity
@Table(name = "fiestas")
public class Fiesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String nombre;
    private String nombreEn;

    @Column(length = 600)
    private String descripcion;
    @Column(length = 600)
    private String descripcionEn;

    @Column(length = 1500)
    private String detalle;
    @Column(length = 1500)
    private String detalleEn;

    private String imagen;
    private String video;
    private boolean popular = false;
    private Integer orden = 0;

    public Fiesta() {}

    public Fiesta(String slug, String nombre, String nombreEn,
                  String descripcion, String descripcionEn,
                  String detalle, String detalleEn,
                  String imagen, boolean popular, Integer orden) {
        this.slug = slug;
        this.nombre = nombre;
        this.nombreEn = nombreEn;
        this.descripcion = descripcion;
        this.descripcionEn = descripcionEn;
        this.detalle = detalle;
        this.detalleEn = detalleEn;
        this.imagen = imagen;
        this.popular = popular;
        this.orden = orden;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNombreEn() { return nombreEn; }
    public void setNombreEn(String nombreEn) { this.nombreEn = nombreEn; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getDescripcionEn() { return descripcionEn; }
    public void setDescripcionEn(String descripcionEn) { this.descripcionEn = descripcionEn; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getDetalleEn() { return detalleEn; }
    public void setDetalleEn(String detalleEn) { this.detalleEn = detalleEn; }
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
