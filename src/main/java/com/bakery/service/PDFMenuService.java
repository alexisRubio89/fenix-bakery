package com.bakery.service;

import com.bakery.model.MenuItem;
import com.bakery.model.MenuItemRepository;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera el menú en PDF replicando exactamente el diseño aprobado:
 * fondo negro, portada con logo, tipografías Cormorant Garamond + Inter,
 * items bilingües con precio dorado, separadores, pie de página numerado
 * y página final de locaciones.
 *
 * Los precios y productos se leen en vivo desde la base de datos.
 */
@Service
public class PDFMenuService {

    private final MenuItemRepository menuItemRepository;

    public PDFMenuService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    // ── Paleta (idéntica al sitio) ──
    private static final DeviceRgb BLACK        = new DeviceRgb(8, 8, 8);
    private static final DeviceRgb GOLD         = new DeviceRgb(201, 168, 76);
    private static final DeviceRgb GOLD_LT      = new DeviceRgb(228, 201, 126);
    private static final DeviceRgb GOLD_DK      = new DeviceRgb(154, 122, 46);
    private static final DeviceRgb WHITE        = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb MUTED        = new DeviceRgb(168, 162, 154);
    private static final DeviceRgb BORDER       = new DeviceRgb(42, 38, 32);
    private static final DeviceRgb DESC_ES      = new DeviceRgb(196, 191, 181);
    private static final DeviceRgb DESC_EN      = new DeviceRgb(138, 132, 120);

    // ── Medidas (1 mm = 2.8346 pt) ──
    private static final float MM = 2.8346457f;
    private static final float PAGE_W = PageSize.LETTER.getWidth();
    private static final float PAGE_H = PageSize.LETTER.getHeight();
    private static final float MARGIN = 20 * MM;
    private static final float BOTTOM_LIMIT = 24 * MM;

    // ── Locaciones ──
    private static final String[][] LOCATIONS = {
        {"Union City",    "4211 Bergenline Ave, Union City, NJ 07087",     "+1 (201) 864-2699", "Lun-Sáb: 5:00am-8:30pm · Dom: 6:00am-8:30pm"},
        {"West New York", "6132 Bergenline Ave, West New York, NJ 07093",  "+1 (201) 854-2262", "Lun-Sáb: 5:00am-8:00pm · Dom: 6:00am-7:00pm"},
        {"North Bergen",  "8133 Bergenline Ave, North Bergen, NJ 07047",   "+1 (201) 994-4060", "Lun-Jue: 6:00am-8:30pm · Vie-Sáb: 6:00am-9:00pm"}
    };

    // Fuentes
    private PdfFont cormorantReg, cormorantSemi, cormorantItalic;
    private PdfFont interReg, interSemi, interBold;

    // Estado del render
    private PdfDocument pdfDoc;
    private PdfCanvas canvas;
    private int pageNum;
    private int totalPages;
    private float y;

    public byte[] generateMenuPDF(String locale) throws Exception {
        List<CategoryGroup> menu = loadMenuFromDatabase();

        // Pase 1: contar páginas reales
        this.totalPages = 1;
        int counted = render(new ByteArrayOutputStream(), menu, false);

        // Pase 2: render final con el total correcto
        this.totalPages = counted;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        render(out, menu, true);
        return out.toByteArray();
    }

    // ─────────────────────────────────────────────
    //  Carga de datos
    // ─────────────────────────────────────────────

    private static class CategoryGroup {
        String nombreEs, nombreEn;
        List<MenuItem> items = new ArrayList<>();
        CategoryGroup(String es, String en) { this.nombreEs = es; this.nombreEn = en; }
    }

    /** Agrupa por categoría respetando el mismo orden que usa el sitio web. */
    private List<CategoryGroup> loadMenuFromDatabase() {
        List<MenuItem> items = menuItemRepository.findAllByOrderByOrdenAsc();

        Map<String, CategoryGroup> grouped = new LinkedHashMap<>();
        for (MenuItem it : items) {
            String catEs = it.getCategoria() == null ? "" : it.getCategoria();
            grouped.computeIfAbsent(catEs, k -> new CategoryGroup(catEs, it.getCategoriaEn()))
                   .items.add(it);
        }
        return new ArrayList<>(grouped.values());
    }

    private void loadFonts() throws Exception {
        cormorantReg    = loadFont("fonts/CormorantGaramond-Regular.ttf");
        cormorantSemi   = loadFont("fonts/CormorantGaramond-SemiBold.ttf");
        cormorantItalic = loadFont("fonts/CormorantGaramond-Italic.ttf");
        interReg        = loadFont("fonts/Inter-Regular.ttf");
        interSemi       = loadFont("fonts/Inter-SemiBold.ttf");
        interBold       = loadFont("fonts/Inter-Bold.ttf");
    }

    private PdfFont loadFont(String path) throws Exception {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return PdfFontFactory.createFont(is.readAllBytes(), PdfEncodings.IDENTITY_H);
        }
    }

    // ─────────────────────────────────────────────
    //  Render
    // ─────────────────────────────────────────────

    private int render(ByteArrayOutputStream out, List<CategoryGroup> menu, boolean isFinal) throws Exception {
        pdfDoc = new PdfDocument(new PdfWriter(out));
        pdfDoc.setDefaultPageSize(PageSize.LETTER);

        // Importante: las PdfFont quedan ligadas al PdfDocument en el que se usan,
        // así que hay que crearlas de nuevo en cada pase.
        loadFonts();

        pageNum = 1;
        newPage();
        y = drawHeader();

        for (CategoryGroup group : menu) {
            if (group.items.isEmpty()) continue;

            // ¿Cabe el título + al menos un item?
            if (y - 19 * MM - itemHeight(group.items.get(0)) < BOTTOM_LIMIT) {
                drawFooter();
                pageNum++;
                newPage();
                y = drawHeader();
            }
            y = drawCategoryTitle(group);

            for (MenuItem item : group.items) {
                if (y - itemHeight(item) < BOTTOM_LIMIT) {
                    drawFooter();
                    pageNum++;
                    newPage();
                    y = drawHeader();
                }
                y = drawItem(item);
            }
        }
        drawFooter();

        // Página final de locaciones
        pageNum++;
        newPage();
        drawLocationsPage();
        drawFooter();

        int total = pageNum;
        pdfDoc.close();
        return total;
    }

    /** Crea una página nueva y le pinta el fondo negro completo. */
    private void newPage() {
        PdfPage page = pdfDoc.addNewPage();
        canvas = new PdfCanvas(page);
        canvas.saveState()
              .setFillColor(BLACK)
              .rectangle(new Rectangle(0, 0, PAGE_W, PAGE_H))
              .fill()
              .restoreState();
    }

    private float drawHeader() throws Exception {
        if (pageNum == 1) {
            // ── Portada ──
            float logoSize = 32 * MM;
            float logoX = (PAGE_W - logoSize) / 2f;
            float logoY = PAGE_H - MARGIN - logoSize + 8 * MM;
            try (InputStream is = new ClassPathResource("static/img/logo.png").getInputStream()) {
                ImageData logo = ImageDataFactory.create(is.readAllBytes());
                canvas.addImageFittedIntoRectangle(logo, new Rectangle(logoX, logoY, logoSize, logoSize), false);
            } catch (Exception ignored) { /* si falta el logo, continúa sin él */ }

            float cy = logoY - 6 * MM;
            drawCentered(spaced("DULCERÍA & PANADERÍA CUBANA"), interSemi, 9, GOLD, cy);

            cy -= 4.5f * MM;
            drawCentered(spaced("CUBAN BAKERY & PASTRY SHOP"), interReg, 7.5f, GOLD_DK, cy);

            cy -= 13 * MM;
            drawCentered("Fenix Bakery", cormorantSemi, 34, WHITE, cy);

            cy -= 8 * MM;
            drawCentered("Nuestro Menú  ·  Our Menu", cormorantItalic, 16, GOLD_LT, cy);

            cy -= 10 * MM;
            line(PAGE_W / 2 - 20 * MM, cy, PAGE_W / 2 + 20 * MM, cy, GOLD, 0.6f);

            return cy - 10 * MM;
        } else {
            // ── Encabezado de páginas interiores ──
            float hy = PAGE_H - MARGIN;
            drawText("Fenix Bakery", cormorantSemi, 15, GOLD, MARGIN, hy);
            drawRight("elfenixbakery.com", interReg, 8, MUTED, PAGE_W - MARGIN, hy);
            line(MARGIN, hy - 4 * MM, PAGE_W - MARGIN, hy - 4 * MM, BORDER, 0.5f);
            return hy - 19 * MM;
        }
    }

    private void drawFooter() throws Exception {
        String txt = "Fenix Bakery  ·  Union City · West New York · North Bergen, NJ  ·  Página "
                   + pageNum + " de " + totalPages;
        drawCentered(txt, interReg, 7.5f, MUTED, 12 * MM);
        line(MARGIN, 17 * MM, PAGE_W - MARGIN, 17 * MM, BORDER, 0.5f);
    }

    private float drawCategoryTitle(CategoryGroup g) throws Exception {
        float cy = y - 6 * MM;                          // respiro superior
        line(MARGIN, cy, MARGIN + 8 * MM, cy, GOLD, 0.6f);
        drawText(g.nombreEs, cormorantSemi, 18, GOLD, MARGIN + 11 * MM, cy - 2.2f * MM);
        float w = cormorantSemi.getWidth(g.nombreEs, 18);
        if (g.nombreEn != null && !g.nombreEn.isBlank()) {
            drawText("/ " + g.nombreEn, cormorantItalic, 13, MUTED,
                     MARGIN + 11 * MM + w + 3 * MM, cy - 2.0f * MM);
        }
        return cy - 13 * MM;                            // respiro inferior
    }

    private float drawItem(MenuItem item) throws Exception {
        float maxW = PAGE_W - 2 * MARGIN - 22 * MM;
        float cy = y;

        String nombreEs = nz(item.getNombre());
        String nombreEn = nz(item.getNombreEn());
        String descEs   = nz(item.getDescripcion());
        String descEn   = nz(item.getDescripcionEn());
        String precio   = nz(item.getPrecio());

        drawText(nombreEs, interSemi, 10.5f, WHITE, MARGIN, cy);
        drawRight(precio, interBold, 10.5f, GOLD, PAGE_W - MARGIN, cy);

        cy -= 4.3f * MM;
        if (!nombreEn.isEmpty()) drawText(nombreEn, interReg, 8, MUTED, MARGIN, cy);

        cy -= 4.6f * MM;
        if (!descEs.isEmpty()) cy = drawWrapped(descEs, MARGIN, cy, maxW, interReg, 8.3f, 3.6f * MM, DESC_ES);
        if (!descEn.isEmpty()) cy = drawWrapped(descEn, MARGIN, cy, maxW, interReg, 7.6f, 3.3f * MM, DESC_EN);

        cy -= 3.2f * MM;
        line(MARGIN, cy, PAGE_W - MARGIN, cy, BORDER, 0.4f);
        return cy - 6.5f * MM;
    }

    /** Altura que ocupará un item (para decidir el salto de página). */
    private float itemHeight(MenuItem item) {
        float maxW = PAGE_W - 2 * MARGIN - 22 * MM;
        int linesEs = countLines(nz(item.getDescripcion()), interReg, 8.3f, maxW);
        int linesEn = countLines(nz(item.getDescripcionEn()), interReg, 7.6f, maxW);
        return 4.3f * MM + 4.6f * MM + linesEs * 3.6f * MM + linesEn * 3.3f * MM + 3.2f * MM + 6.5f * MM;
    }

    private void drawLocationsPage() throws Exception {
        float cy = drawHeader();

        cy -= 4 * MM;
        line(PAGE_W / 2 - 15 * MM, cy, PAGE_W / 2 + 15 * MM, cy, GOLD, 0.6f);
        cy -= 10 * MM;

        drawCentered("Nuestras Locaciones  ·  Our Locations", cormorantSemi, 22, WHITE, cy);
        cy -= 8 * MM;
        drawCentered("New Jersey — Union City · West New York · North Bergen", interReg, 9, MUTED, cy);
        cy -= 16 * MM;

        for (String[] loc : LOCATIONS) {
            float boxH = 26 * MM;
            roundRect(MARGIN, cy - boxH, PAGE_W - 2 * MARGIN, boxH, 3, BORDER, 0.6f);

            float iy = cy - 8 * MM;
            drawText(loc[0], cormorantSemi, 16, GOLD, MARGIN + 8 * MM, iy);
            iy -= 7 * MM;
            drawText(loc[1], interReg, 9, WHITE, MARGIN + 8 * MM, iy);
            iy -= 5.5f * MM;
            drawText("Tel: " + loc[2] + "   ·   " + loc[3], interReg, 8.5f, MUTED, MARGIN + 8 * MM, iy);

            cy = cy - boxH - 8 * MM;
        }

        cy -= 6 * MM;
        drawCentered("elfenixbakery.com", cormorantItalic, 13, GOLD_LT, cy);
    }

    // ─────────────────────────────────────────────
    //  Primitivas de dibujo
    // ─────────────────────────────────────────────

    private void drawText(String txt, PdfFont font, float size, DeviceRgb color, float x, float yy) throws Exception {
        canvas.saveState().beginText().setFontAndSize(font, size).setFillColor(color)
              .moveText(x, yy).showText(txt).endText().restoreState();
    }

    private void drawCentered(String txt, PdfFont font, float size, DeviceRgb color, float yy) throws Exception {
        float w = font.getWidth(txt, size);
        drawText(txt, font, size, color, (PAGE_W - w) / 2f, yy);
    }

    private void drawRight(String txt, PdfFont font, float size, DeviceRgb color, float rightX, float yy) throws Exception {
        float w = font.getWidth(txt, size);
        drawText(txt, font, size, color, rightX - w, yy);
    }

    /** Dibuja texto con salto de línea automático. Devuelve la Y final. */
    private float drawWrapped(String text, float x, float yy, float maxWidth,
                              PdfFont font, float size, float lineH, DeviceRgb color) throws Exception {
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (font.getWidth(test, size) <= maxWidth) {
                line.setLength(0);
                line.append(test);
            } else {
                drawText(line.toString(), font, size, color, x, yy);
                yy -= lineH;
                line.setLength(0);
                line.append(word);
            }
        }
        if (line.length() > 0) {
            drawText(line.toString(), font, size, color, x, yy);
            yy -= lineH;
        }
        return yy;
    }

    private int countLines(String text, PdfFont font, float size, float maxWidth) {
        if (text.isEmpty()) return 0;
        int count = 1;
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (font.getWidth(test, size) <= maxWidth) {
                line.setLength(0);
                line.append(test);
            } else {
                count++;
                line.setLength(0);
                line.append(word);
            }
        }
        return count;
    }

    private void line(float x1, float y1, float x2, float y2, DeviceRgb color, float width) {
        canvas.saveState().setStrokeColor(color).setLineWidth(width)
              .moveTo(x1, y1).lineTo(x2, y2).stroke().restoreState();
    }

    private void roundRect(float x, float yy, float w, float h, float r, DeviceRgb color, float width) {
        canvas.saveState().setStrokeColor(color).setLineWidth(width)
              .roundRectangle(x, yy, w, h, r).stroke().restoreState();
    }

    /** Simula letter-spacing insertando espacios entre caracteres. */
    private String spaced(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    private String nz(String s) { return s == null ? "" : s.trim(); }
}
