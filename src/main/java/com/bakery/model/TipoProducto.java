package com.bakery.model;

/**
 * División principal del almacén.
 * INGREDIENTE: entra en la receta (harina, huevos, guayaba...).
 * INSUMO: no comestible, sirve para servir o empacar (vasos, cajas, servilletas...).
 */
public enum TipoProducto {

    INGREDIENTE("Ingrediente", "🥣"),
    INSUMO("Insumo", "📦");

    private final String etiqueta;
    private final String icono;

    TipoProducto(String etiqueta, String icono) {
        this.etiqueta = etiqueta;
        this.icono = icono;
    }

    public String getEtiqueta() { return etiqueta; }
    public String getIcono()    { return icono; }
}
