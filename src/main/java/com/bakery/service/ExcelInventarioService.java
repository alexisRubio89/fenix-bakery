package com.bakery.service;

import com.bakery.model.Producto;
import com.bakery.model.TipoProducto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera el reporte de almacén en Excel, respetando el formato de la hoja
 * "Productos Almacén" que ya se usaba a mano: título azul oscuro, encabezados
 * azules, Calibri y las columnas CAJAS / UNIDAD.
 *
 * Hoja 1 — existencia completa de todo el almacén.
 * Hoja 2 — solo lo agotado o por debajo del mínimo, para salir a comprar.
 */
@Service
public class ExcelInventarioService {

    // Colores tomados de la hoja original
    private static final String AZUL_TITULO = "1B3A6B";
    private static final String AZUL_CABECERA = "2E5FA3";
    private static final String GRIS_CATEGORIA = "DCE6F1";
    private static final String ROJO_AGOTADO = "F8CBCB";
    private static final String AMBAR_BAJO = "FCE4B2";

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generar(List<Producto> todos) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Estilos e = new Estilos(wb);

            List<Producto> reponer = todos.stream()
                    .filter(Producto::isStockBajo)
                    .sorted(Comparator.comparing(Producto::isAgotado).reversed()
                            .thenComparing(p -> p.getNombre() == null ? "" : p.getNombre()))
                    .toList();

            hojaExistencia(wb, e, todos);
            hojaReposicion(wb, e, reponer);

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ══ HOJA 1 — Existencia ══════════════════════════════════════
    private void hojaExistencia(Workbook wb, Estilos e, List<Producto> productos) {
        Sheet sh = wb.createSheet("Existencia");
        anchos(sh);

        int fila = encabezado(sh, e, "Productos Almacén",
                "NOMBRE / DESCRIPCIÓN DEL PRODUCTO", "CAJAS", "UNIDAD", 4);

        // Agrupado por tipo y categoría para que 84 productos se puedan recorrer
        for (Map.Entry<String, List<Producto>> grupo : agrupar(productos).entrySet()) {
            fila = filaCategoria(sh, e, fila, grupo.getKey());

            for (Producto p : grupo.getValue()) {
                Row r = sh.createRow(fila++);
                r.setHeightInPoints(18);

                celdaTexto(r, 1, nombreConUnidad(p), e.nombre);
                celdaNumero(r, 2, cajasDe(p), e.dato);
                celdaNumero(r, 3, sueltasDe(p), e.dato);
            }
        }

        totalGeneral(sh, e, fila, productos);
        impresion(sh);
    }

    // ══ HOJA 2 — Hay que reponer ═════════════════════════════════
    private void hojaReposicion(Workbook wb, Estilos e, List<Producto> reponer) {
        Sheet sh = wb.createSheet("Hay que reponer");
        anchos(sh);
        sh.setColumnWidth(4, col(16));   // columna extra: mínimo
        sh.setColumnWidth(5, col(14));   // columna extra: estado
        sh.setColumnWidth(6, col(2));

        int fila = encabezado(sh, e, "Productos por reponer",
                "NOMBRE / DESCRIPCIÓN DEL PRODUCTO", "CAJAS", "UNIDAD", 6);
        // Cabeceras de las dos columnas añadidas, en la misma fila 5 (índice 4)
        Row cab = sh.getRow(4);
        celdaTexto(cab, 4, "MÍNIMO", e.cabecera);
        celdaTexto(cab, 5, "ESTADO", e.cabecera);

        if (reponer.isEmpty()) {
            Row r = sh.createRow(fila);
            r.setHeightInPoints(18);
            celdaTexto(r, 1, "Todo el almacén está por encima del mínimo.", e.dato);
            impresion(sh);
            return;
        }

        for (Producto p : reponer) {
            Row r = sh.createRow(fila++);
            r.setHeightInPoints(18);

            boolean agotado = p.isAgotado();
            CellStyle stNombre = agotado ? e.nombreAgotado : e.nombreBajo;
            CellStyle stDato   = agotado ? e.datoAgotado   : e.datoBajo;

            celdaTexto(r, 1, nombreConUnidad(p), stNombre);
            celdaNumero(r, 2, cajasDe(p), stDato);
            celdaNumero(r, 3, sueltasDe(p), stDato);
            celdaTexto(r, 4, minimoTexto(p), stDato);
            celdaTexto(r, 5, agotado ? "AGOTADO" : "Bajo", stDato);
        }

        Row tot = sh.createRow(fila + 1);
        tot.setHeightInPoints(18);
        celdaTexto(tot, 1, "Productos por reponer: " + reponer.size(), e.total);
        impresion(sh);
    }

    // ══ Bloque de título y cabeceras, idéntico al original ═══════
    private int encabezado(Sheet sh, Estilos e, String titulo, String c1, String c2, String c3,
                           int ultimaColumna) {
        // Fila 1: título sobre banda azul oscura, a todo el ancho de la hoja
        Row r1 = sh.createRow(0);
        r1.setHeightInPoints(25);
        for (int i = 0; i <= ultimaColumna; i++) celdaTexto(r1, i, i == 0 ? titulo : "", e.titulo);
        sh.addMergedRegion(new CellRangeAddress(0, 0, 0, ultimaColumna));

        // Fila 2: FECHA:
        Row r2 = sh.createRow(1);
        celdaTexto(r2, 3, "FECHA:", e.negrita);

        // Fila 3: la fecha de generación, donde la hoja original la pedía a mano
        Row r3 = sh.createRow(2);
        celdaTexto(r3, 1, "Generado el →", e.negritaDerecha);
        celdaTexto(r3, 2, LocalDate.now().format(FECHA), e.fecha);
        sh.addMergedRegion(new CellRangeAddress(2, 2, 2, 3));

        // Fila 5: cabeceras de columna
        Row r5 = sh.createRow(4);
        r5.setHeightInPoints(20);
        celdaTexto(r5, 1, c1, e.cabecera);
        celdaTexto(r5, 2, c2, e.cabecera);
        celdaTexto(r5, 3, c3, e.cabecera);

        return 5;   // primera fila de datos
    }

    private int filaCategoria(Sheet sh, Estilos e, int fila, String etiqueta) {
        Row r = sh.createRow(fila);
        r.setHeightInPoints(18);
        for (int i = 1; i <= 3; i++) celdaTexto(r, i, i == 1 ? etiqueta : "", e.categoria);
        sh.addMergedRegion(new CellRangeAddress(fila, fila, 1, 3));
        return fila + 1;
    }

    private void totalGeneral(Sheet sh, Estilos e, int fila, List<Producto> productos) {
        long conExistencia = productos.stream().filter(p -> !p.isAgotado()).count();
        Row r = sh.createRow(fila + 1);
        r.setHeightInPoints(18);
        celdaTexto(r, 1, "Productos en el almacén: " + productos.size()
                + "   ·   con existencia: " + conExistencia
                + "   ·   por reponer: " + productos.stream().filter(Producto::isStockBajo).count(),
                e.total);
    }

    // ══ Datos derivados ══════════════════════════════════════════

    /** Los productos que no se cuentan por caja llevan su unidad entre paréntesis. */
    private String nombreConUnidad(Producto p) {
        String nombre = p.getNombre() != null ? p.getNombre() : "";
        if (p.isPorCaja() || p.getUnidad() == null) return nombre;
        return nombre + " (" + p.getUnidad().getEtiqueta().toLowerCase() + ")";
    }

    /** Columna CAJAS: cajas cerradas, o la cantidad directa si no hay desglose. */
    private Double cajasDe(Producto p) {
        if (p.isPorCaja()) return (double) p.getCajasCompletas();
        return p.getCantidad() != null ? p.getCantidad().doubleValue() : 0d;
    }

    /** Columna UNIDAD: sueltas de la caja empezada. Vacía si no hay desglose. */
    private Double sueltasDe(Producto p) {
        if (!p.isPorCaja()) return null;
        return new BigDecimal(p.getUnidadesSueltas()).doubleValue();
    }

    private String minimoTexto(Producto p) {
        String min = p.getStockMinimoTexto();
        return p.isPorCaja() ? min + " u" : min + " " + p.getUnidad().getAbreviatura();
    }

    /** Ordena por tipo (ingredientes primero) y agrupa por categoría. */
    private Map<String, List<Producto>> agrupar(List<Producto> productos) {
        List<Producto> ordenados = new ArrayList<>(productos);
        ordenados.sort(Comparator
                .comparing((Producto p) -> p.getTipo() == TipoProducto.INSUMO ? 1 : 0)
                .thenComparing(p -> p.getCategoria() == null ? "" : p.getCategoria())
                .thenComparing(p -> p.getNombre() == null ? "" : p.getNombre()));

        // Con un solo tipo en el reporte, repetir "INSUMO ·" en cada franja sobra
        boolean variosTipos = productos.stream()
                .map(Producto::getTipo).distinct().count() > 1;

        Map<String, List<Producto>> mapa = new LinkedHashMap<>();
        for (Producto p : ordenados) {
            String categoria = p.getCategoria() != null ? p.getCategoria() : "Sin categoría";
            String clave = categoria;
            if (variosTipos) {
                String tipo = p.getTipo() != null ? p.getTipo().getEtiqueta().toUpperCase() : "SIN TIPO";
                clave = tipo + "  ·  " + categoria;
            }
            mapa.computeIfAbsent(clave, k -> new ArrayList<>()).add(p);
        }
        return mapa;
    }

    // ══ Utilidades de celda ══════════════════════════════════════
    private void celdaTexto(Row r, int col, String valor, CellStyle estilo) {
        Cell c = r.createCell(col);
        c.setCellValue(valor);
        if (estilo != null) c.setCellStyle(estilo);
    }

    private void celdaNumero(Row r, int col, Double valor, CellStyle estilo) {
        Cell c = r.createCell(col);
        if (valor != null) c.setCellValue(valor);
        c.setCellStyle(estilo);
    }

    private void anchos(Sheet sh) {
        sh.setColumnWidth(0, col(2));       // margen izquierdo
        sh.setColumnWidth(1, col(55));      // nombre
        sh.setColumnWidth(2, col(13));      // cajas
        sh.setColumnWidth(3, col(14));      // unidad
        sh.setColumnWidth(4, col(2));       // margen derecho
    }

    /**
     * Preparación para imprimir: la hoja se usa en papel para hacer el conteo,
     * así que se ajusta a un ancho de página y el bloque de cabecera se repite
     * arriba en cada hoja impresa. El panel congelado deja los títulos fijos
     * al desplazarse por los 84 productos.
     */
    private void impresion(Sheet sh) {
        sh.setFitToPage(true);
        PrintSetup ps = sh.getPrintSetup();
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);          // tantas páginas de alto como haga falta
        ps.setPaperSize(PrintSetup.LETTER_PAPERSIZE);
        sh.setRepeatingRows(CellRangeAddress.valueOf("1:5"));
        sh.createFreezePane(0, 5);
        sh.setMargin(Sheet.LeftMargin, 0.4);
        sh.setMargin(Sheet.RightMargin, 0.4);
    }

    /** POI mide el ancho en 1/256 de carácter. */
    private int col(double caracteres) {
        return (int) (caracteres * 256);
    }

    // ══ Estilos, creados una sola vez por libro ══════════════════
    private static class Estilos {
        final CellStyle titulo, cabecera, nombre, dato, categoria, negrita, negritaDerecha, fecha, total;
        final CellStyle nombreAgotado, datoAgotado, nombreBajo, datoBajo;

        Estilos(Workbook wb) {
            titulo = base(wb, 18, true, IndexedColors.WHITE.getIndex());
            fondo(titulo, AZUL_TITULO);
            titulo.setAlignment(HorizontalAlignment.CENTER);
            titulo.setVerticalAlignment(VerticalAlignment.CENTER);

            cabecera = base(wb, 12, true, IndexedColors.WHITE.getIndex());
            fondo(cabecera, AZUL_CABECERA);
            cabecera.setAlignment(HorizontalAlignment.CENTER);
            cabecera.setVerticalAlignment(VerticalAlignment.CENTER);
            bordes(cabecera);

            nombre = base(wb, 12, true, null);
            nombre.setAlignment(HorizontalAlignment.LEFT);
            nombre.setVerticalAlignment(VerticalAlignment.CENTER);
            bordes(nombre);

            dato = base(wb, 11, false, null);
            dato.setAlignment(HorizontalAlignment.CENTER);
            dato.setVerticalAlignment(VerticalAlignment.CENTER);
            bordes(dato);

            categoria = base(wb, 11, true, null);
            fondo(categoria, GRIS_CATEGORIA);
            categoria.setAlignment(HorizontalAlignment.LEFT);
            categoria.setVerticalAlignment(VerticalAlignment.CENTER);
            bordes(categoria);

            negrita = base(wb, 11, true, null);

            negritaDerecha = base(wb, 11, true, null);
            negritaDerecha.setAlignment(HorizontalAlignment.RIGHT);

            fecha = base(wb, 11, false, null);
            fecha.setAlignment(HorizontalAlignment.CENTER);

            total = base(wb, 11, false, null);
            ((org.apache.poi.xssf.usermodel.XSSFCellStyle) total).getFont().setItalic(true);

            nombreAgotado = copiar(wb, nombre); fondo(nombreAgotado, ROJO_AGOTADO);
            datoAgotado   = copiar(wb, dato);   fondo(datoAgotado, ROJO_AGOTADO);
            nombreBajo    = copiar(wb, nombre); fondo(nombreBajo, AMBAR_BAJO);
            datoBajo      = copiar(wb, dato);   fondo(datoBajo, AMBAR_BAJO);
        }

        private static CellStyle base(Workbook wb, int puntos, boolean negrita, Short color) {
            CellStyle st = wb.createCellStyle();
            Font f = wb.createFont();
            f.setFontName("Calibri");
            f.setFontHeightInPoints((short) puntos);
            f.setBold(negrita);
            if (color != null) f.setColor(color);
            st.setFont(f);
            return st;
        }

        private static CellStyle copiar(Workbook wb, CellStyle origen) {
            CellStyle st = wb.createCellStyle();
            st.cloneStyleFrom(origen);
            return st;
        }

        /**
         * El color exacto solo se puede fijar por RGB, y eso exige XSSFCellStyle:
         * la interfaz CellStyle únicamente admite los colores indexados clásicos.
         */
        private static void fondo(CellStyle st, String hex) {
            byte[] rgb = new byte[] {
                    (byte) Integer.parseInt(hex.substring(0, 2), 16),
                    (byte) Integer.parseInt(hex.substring(2, 4), 16),
                    (byte) Integer.parseInt(hex.substring(4, 6), 16)
            };
            ((org.apache.poi.xssf.usermodel.XSSFCellStyle) st)
                    .setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(rgb, null));
            st.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        private static void bordes(CellStyle st) {
            st.setBorderTop(BorderStyle.THIN);
            st.setBorderBottom(BorderStyle.THIN);
            st.setBorderLeft(BorderStyle.THIN);
            st.setBorderRight(BorderStyle.THIN);
        }
    }
}
