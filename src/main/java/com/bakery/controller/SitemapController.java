package com.bakery.controller;

import com.bakery.service.CakeService;
import com.bakery.service.FiestaService;
import com.bakery.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SitemapController {

    private static final String BASE = "https://elfenixbakery.com";

    @Autowired private MenuService menuService;
    @Autowired private CakeService cakeService;
    @Autowired private FiestaService fiestaService;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // Páginas estáticas principales
        addUrl(sb, "/", "1.0", "weekly");
        addUrl(sb, "/menu", "0.9", "weekly");
        addUrl(sb, "/cakes", "0.9", "weekly");
        addUrl(sb, "/fiestas", "0.9", "weekly");
        addUrl(sb, "/nosotros", "0.7", "monthly");
        addUrl(sb, "/contacto", "0.8", "monthly");

        // Locaciones
        addUrl(sb, "/locaciones/union-city", "0.8", "monthly");
        addUrl(sb, "/locaciones/west-new-york", "0.8", "monthly");
        addUrl(sb, "/locaciones/north-bergen", "0.8", "monthly");

        // Productos del menú
        menuService.obtenerTodos().forEach(p ->
            addUrl(sb, "/menu/" + p.getSlug(), "0.6", "monthly"));

        // Cakes
        cakeService.obtenerTodos().forEach(c ->
            addUrl(sb, "/cakes/" + c.getSlug(), "0.6", "monthly"));

        // Fiestas
        fiestaService.obtenerTodos().forEach(f ->
            addUrl(sb, "/fiestas/" + f.getSlug(), "0.6", "monthly"));

        sb.append("</urlset>");
        return sb.toString();
    }

    private void addUrl(StringBuilder sb, String path, String priority, String freq) {
        sb.append("  <url>\n")
          .append("    <loc>").append(BASE).append(path).append("</loc>\n")
          .append("    <changefreq>").append(freq).append("</changefreq>\n")
          .append("    <priority>").append(priority).append("</priority>\n")
          .append("  </url>\n");
    }
}
