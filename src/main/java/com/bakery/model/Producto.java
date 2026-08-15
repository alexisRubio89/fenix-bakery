package com.bakery.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false)
    private String nombre;

    /** División principal: ingrediente de receta o insumo no comestible. */
    @NotNull(message = "El tipo es requerido")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoProducto tipo;

    /** Subclasificación dentro del tipo (Harinas y granos, Vasos y tapas...). */
    @NotBlank(message = "La categoría es requerida")
    private String categoria;

    /**
     * Existencia total, expresada SIEMPRE en la unidad base del producto.
     * Cuando hay unidadesPorCaja, la cifra son unidades sueltas: 1037 se
     * presenta como "2 cajas + 37". Cuando no la hay, son cajas/galones/libras
     * directamente, y admite medios (1.5) para la caja empezada.
     */
    @NotNull(message = "La cantidad es requerida")
    @DecimalMin(value = "0.0", message = "La cantidad no puede ser negativa")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad = BigDecimal.ZERO;

    @NotNull(message = "La unidad es requerida")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnidadMedida unidad = UnidadMedida.UNIDAD;

    /**
     * Punto de reposición propio de cada producto: un saco de harina y un paquete
     * de servilletas no se reponen con el mismo número.
     */
    @NotNull(message = "El stock mínimo es requerido")
    @DecimalMin(value = "0.0", message = "El stock mínimo no puede ser negativo")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    /**
     * Cuántas unidades sueltas trae una caja (o paquete) cerrada.
     * Si está vacío, el producto se cuenta directamente en su unidad
     * (galones de cloro, libras de manteca) y no hay desglose.
     */
    @Min(value = 1, message = "Las unidades por caja deben ser al menos 1")
    private Integer unidadesPorCaja;

    /** Proveedor habitual, para armar la solicitud de compra. */
    private String proveedor;

    /** Código con el que el proveedor identifica el producto en su catálogo. */
    private String codigoProveedor;

    /**
     * Costo por unidad. Opcional: se puede dar de alta un producto y completar
     * el precio después, sin inventar cifras que falseen el valor del almacén.
     */
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private Double precio;

    private String descripcion;

    // ── Constructores ──────────────────────────────
    public Producto() {}

    public Producto(String nombre, TipoProducto tipo, String categoria, BigDecimal cantidad,
                    UnidadMedida unidad, BigDecimal stockMinimo, Double precio, String descripcion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.stockMinimo = stockMinimo;
        this.precio = precio;
        this.descripcion = descripcion;
    }

    /** Alta desde el catálogo de un proveedor. */
    public Producto(String nombre, TipoProducto tipo, String categoria, UnidadMedida unidad,
                    String proveedor, String codigoProveedor) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.categoria = categoria;
        this.unidad = unidad;
        this.proveedor = proveedor;
        this.codigoProveedor = codigoProveedor;
        this.cantidad = BigDecimal.ZERO;
        this.stockMinimo = BigDecimal.ZERO;
    }

    // ── Getters y Setters ──────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoProducto getTipo() { return tipo; }
    public void setTipo(TipoProducto tipo) { this.tipo = tipo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public UnidadMedida getUnidad() { return unidad; }
    public void setUnidad(UnidadMedida unidad) { this.unidad = unidad; }

    public BigDecimal getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(BigDecimal stockMinimo) { this.stockMinimo = stockMinimo; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Integer getUnidadesPorCaja() { return unidadesPorCaja; }
    public void setUnidadesPorCaja(Integer unidadesPorCaja) { this.unidadesPorCaja = unidadesPorCaja; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public String getCodigoProveedor() { return codigoProveedor; }
    public void setCodigoProveedor(String codigoProveedor) { this.codigoProveedor = codigoProveedor; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    // ── Helpers ───────────────────────────────────

    /**
     * Hay que reponer cuando la existencia llegó al mínimo o bajó de él.
     * Se compara con compareTo: equals() de BigDecimal considera distintos 5 y 5.00.
     */
    @Transient
    public boolean isStockBajo() {
        if (cantidad == null || stockMinimo == null) return false;
        return cantidad.compareTo(stockMinimo) <= 0;
    }

    /** Sin existencia: se distingue del "stock bajo" para poder priorizar la compra. */
    @Transient
    public boolean isAgotado() {
        return cantidad != null && cantidad.compareTo(BigDecimal.ZERO) <= 0;
    }

    /** "5" en vez de "5.00", pero conserva "1.5". Para no ensuciar la tabla. */
    @Transient
    public String getCantidadTexto() {
        return formatear(cantidad);
    }

    @Transient
    public String getStockMinimoTexto() {
        return formatear(stockMinimo);
    }

    /** Cantidad con su unidad: "1.5 caja", "12 u". */
    @Transient
    public String getCantidadConUnidad() {
        String abrev = unidad != null ? unidad.getAbreviatura() : "";
        return (getCantidadTexto() + " " + abrev).trim();
    }

    // ── Desglose en cajas cerradas + unidades sueltas ─────────────

    /** true si este producto se cuenta por caja con desglose de sueltas. */
    @Transient
    public boolean isPorCaja() {
        return unidadesPorCaja != null && unidadesPorCaja > 0;
    }

    /** Cajas completas que hay en almacén. 1037 unidades a 500 por caja → 2. */
    @Transient
    public int getCajasCompletas() {
        if (!isPorCaja() || cantidad == null) return 0;
        return cantidad.divideToIntegralValue(BigDecimal.valueOf(unidadesPorCaja)).intValue();
    }

    /** Sueltas de la caja empezada. 1037 unidades a 500 por caja → 37. */
    @Transient
    public String getUnidadesSueltas() {
        if (!isPorCaja() || cantidad == null) return "0";
        return formatear(cantidad.remainder(BigDecimal.valueOf(unidadesPorCaja)));
    }

    /**
     * Existencia lista para mostrar. Con desglose: "2 cajas + 37".
     * Sin desglose: "1.5 caja". Es lo que va en la tabla del inventario.
     */
    @Transient
    public String getExistenciaTexto() {
        if (!isPorCaja()) return getCantidadConUnidad();

        int cajas = getCajasCompletas();
        String sueltas = getUnidadesSueltas();
        String etiquetaCaja = cajas == 1 ? "caja" : "cajas";

        if ("0".equals(sueltas)) return cajas + " " + etiquetaCaja;
        if (cajas == 0)          return sueltas + " u";
        return cajas + " " + etiquetaCaja + " + " + sueltas;
    }

    /** Total en unidades sueltas, para el detalle: "1037 u en total". */
    @Transient
    public String getTotalUnidadesTexto() {
        return getCantidadTexto() + " u";
    }

    /** Convierte cajas + sueltas al total en unidades que se guarda. */
    public void asignarExistencia(BigDecimal cajas, BigDecimal sueltas) {
        BigDecimal c = cajas   != null ? cajas   : BigDecimal.ZERO;
        BigDecimal s = sueltas != null ? sueltas : BigDecimal.ZERO;
        this.cantidad = isPorCaja()
                ? c.multiply(BigDecimal.valueOf(unidadesPorCaja)).add(s)
                : c;
    }

    /** Valor inmovilizado de esta línea del almacén. Cero si aún no tiene precio. */
    @Transient
    public BigDecimal getValorTotal() {
        if (cantidad == null || precio == null) return BigDecimal.ZERO;
        return cantidad.multiply(BigDecimal.valueOf(precio));
    }

    /** true si falta cargar el precio: la vista lo muestra como "—". */
    @Transient
    public boolean isSinPrecio() {
        return precio == null;
    }

    private String formatear(BigDecimal valor) {
        if (valor == null) return "0";
        return valor.stripTrailingZeros().toPlainString();
    }
}
