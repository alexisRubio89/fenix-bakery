package com.bakery.service;

import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Detecta mensajes de spam en el formulario de contacto usando 3 capas:
 *
 *  1) Texto sin sentido (gibberish): nombres/asuntos/mensajes con palabras
 *     largas sin ninguna vocal, típicos de bots generadores de texto aleatorio
 *     (ej. "mvwkfzmrtg", "pmfwgrryvtyzypfqxvoofdnypdnklp").
 *
 *  2) Dominios de email desechables/temporales conocidos (mailinator, etc.).
 *
 *  3) Spamhaus Domain Block List (DBL): lista de reputación de dominios en
 *     tiempo real, consultada vía DNS (gratis, sin API key). Si el dominio
 *     del remitente aparece listado como spam/phishing, se rechaza.
 *     Referencia: https://www.spamhaus.org/dbl/
 *
 * Cualquier fallo de red al consultar Spamhaus se ignora (fail-open) para no
 * bloquear mensajes legítimos por un problema de DNS temporal.
 */
@Service
public class SpamDetectionService {

    // Vocales de idiomas que puede usar la clientela del sitio (es/en, con acentos)
    private static final Pattern VOWEL = Pattern.compile("[aeiouáéíóúàèìòùäëïöü]", Pattern.CASE_INSENSITIVE);
    private static final Pattern WORD = Pattern.compile("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ]+");
    private static final int MIN_LEN_SIN_VOCAL = 5; // palabras de 5+ letras sin ninguna vocal = sospechosas

    // Dominios de email desechables/temporales más comunes
    private static final Set<String> DOMINIOS_DESECHABLES = Set.of(
        "mailinator.com", "guerrillamail.com", "guerrillamail.info", "10minutemail.com",
        "tempmail.com", "temp-mail.org", "yopmail.com", "trashmail.com", "sharklasers.com",
        "throwawaymail.com", "getnada.com", "dispostable.com", "fakeinbox.com", "mintemail.com",
        "mohmal.com", "moakt.com", "emailondeck.com", "maildrop.cc", "mailnesia.com",
        "spamgourmet.com", "mailcatch.com", "mailtemp.info", "tempinbox.com", "tempr.email",
        "burnermail.io", "mailna.co", "harakirimail.com", "mytemp.email", "instant-email.org",
        "mailsac.com", "inboxbear.com", "tmpmail.net", "tmpeml.com", "0x0.st", "discard.email"
    );

    /** true si el nombre, asunto o mensaje contiene texto sin sentido (bot). */
    public boolean esTextoSinSentido(String... campos) {
        for (String campo : campos) {
            if (campo == null) continue;
            var matcher = WORD.matcher(campo);
            while (matcher.find()) {
                String palabra = matcher.group();
                if (palabra.length() >= MIN_LEN_SIN_VOCAL && !VOWEL.matcher(palabra).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    /** true si el email pertenece a un proveedor desechable/temporal conocido. */
    public boolean esEmailDesechable(String email) {
        String dominio = extraerDominio(email);
        return dominio != null && DOMINIOS_DESECHABLES.contains(dominio);
    }

    /**
     * true si el dominio del email está en la lista de reputación Spamhaus DBL.
     * Consulta DNS: <dominio>.dbl.spamhaus.org — si resuelve a 127.0.1.x, está listado.
     * Si la consulta falla (timeout, sin red), se asume que NO está listado (fail-open).
     */
    public boolean estaEnListaSpamhaus(String email) {
        String dominio = extraerDominio(email);
        if (dominio == null) return false;

        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        env.put("com.sun.jndi.dns.timeout.initial", "1500");  // 1.5s por intento
        env.put("com.sun.jndi.dns.timeout.retries", "1");

        try {
            DirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(dominio + ".dbl.spamhaus.org", new String[]{"A"});
            Attribute a = attrs.get("A");
            ctx.close();
            // Cualquier respuesta 127.0.1.x confirma que está listado
            return a != null && a.size() > 0;
        } catch (NamingException e) {
            // NXDOMAIN = no está listado (caso normal y esperado para dominios limpios)
            return false;
        } catch (Exception e) {
            // Fallo de red/DNS: no bloquear por un problema temporal de infraestructura
            return false;
        }
    }

    /** Chequeo combinado: true si el mensaje debe rechazarse por spam. */
    public boolean esSpam(String nombre, String email, String asunto, String mensaje) {
        if (esTextoSinSentido(nombre, asunto, mensaje)) return true;
        if (esEmailDesechable(email)) return true;
        return estaEnListaSpamhaus(email);
    }

    private String extraerDominio(String email) {
        if (email == null || !email.contains("@")) return null;
        return email.substring(email.indexOf('@') + 1).trim().toLowerCase(Locale.ROOT);
    }
}
