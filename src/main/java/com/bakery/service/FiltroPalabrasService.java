package com.bakery.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FiltroPalabrasService {

    // Lista de palabras a censurar (español e inglés).
    // Se comparan sin distinguir mayúsculas/minúsculas ni acentos.
    private static final List<String> PALABRAS = List.of(
        // ── Español ──
        "mierda", "puta", "puto", "putas", "putos", "cabron", "cabrona", "cabrones",
        "pendejo", "pendeja", "pendejos", "pendejas", "coño", "cono", "joder",
        "gilipollas", "capullo", "maricon", "maricón", "marica", "verga", "vergas",
        "chinga", "chingada", "chingar", "chingate", "culero", "culera", "cojones",
        "carajo", "pinche", "imbecil", "imbécil", "idiota", "estupido", "estúpido",
        "mamon", "mamón", "mamada", "polla", "pollas", "zorra", "zorras", "perra",
        "hijoputa", "hijueputa", "hdp", "malparido", "malparida", "gonorrea",
        "concha", "conchatumadre", "huevon", "huevón", "boludo", "pelotudo",
        "mocoso", "putamadre", "chupame","culo", "chupamela", "cagada", "cagar",
        // ── Inglés ──
        "fuck", "fucking", "fucker", "motherfucker", "shit", "bullshit", "bitch",
        "bitches", "asshole", "ass", "dick", "dickhead", "pussy", "cunt", "bastard",
        "slut", "whore", "dumbass", "jackass", "crap", "damn", "faggot", "retard",
        "nigger", "nigga", "cock", "wanker", "twat","ass", "prick", "douche", "douchebag"
    );

    private final List<Pattern> patrones;

    public FiltroPalabrasService() {
        patrones = PALABRAS.stream()
            .map(p -> Pattern.compile("\\b" + Pattern.quote(p) + "\\b",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))
            .toList();
    }

    /**
     * Censura las palabras prohibidas reemplazando las letras intermedias por asteriscos.
     * Ej: "mierda" -> "m****a", "fuck" -> "f**k"
     */
    public String censurar(String texto) {
        if (texto == null || texto.isBlank()) return texto;
        String resultado = texto;
        for (Pattern patron : patrones) {
            Matcher matcher = patron.matcher(resultado);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(censurarPalabra(matcher.group())));
            }
            matcher.appendTail(sb);
            resultado = sb.toString();
        }
        return resultado;
    }

    private String censurarPalabra(String palabra) {
        if (palabra.length() <= 2) {
            return "*".repeat(palabra.length());
        }
        // Mantiene la primera y última letra, censura el resto
        char primera = palabra.charAt(0);
        char ultima = palabra.charAt(palabra.length() - 1);
        return primera + "*".repeat(palabra.length() - 2) + ultima;
    }
}
