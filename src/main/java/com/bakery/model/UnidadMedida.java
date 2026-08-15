package com.bakery.model;

/**
 * Unidad en la que se cuenta cada producto del almacén.
 * La cantidad admite decimales, así que "media caja" se registra como 1.5 CAJA.
 */
public enum UnidadMedida {

    UNIDAD ("Unidad",   "u"),
    CAJA   ("Caja",     "caja"),
    BOLSA  ("Bolsa",    "bolsa"),
    PAQUETE("Paquete",  "paq"),
    SACO   ("Saco",     "saco"),
    LIBRA  ("Libra",    "lb"),
    ONZA   ("Onza",     "oz"),
    GALON  ("Galón",    "gal"),
    LITRO  ("Litro",    "L"),
    DOCENA ("Docena",   "doc");

    private final String etiqueta;
    private final String abreviatura;

    UnidadMedida(String etiqueta, String abreviatura) {
        this.etiqueta = etiqueta;
        this.abreviatura = abreviatura;
    }

    public String getEtiqueta()    { return etiqueta; }
    public String getAbreviatura() { return abreviatura; }
}
