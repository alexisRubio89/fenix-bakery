package com.bakery.service;

/**
 * Se lanza cuando el formulario de contacto detecta un mensaje como spam
 * (texto sin sentido, email desechable, o dominio listado en Spamhaus).
 * El mensaje NO se guarda en base de datos ni se envía por email.
 */
public class MensajeSpamException extends RuntimeException {
    public MensajeSpamException(String motivo) {
        super("Mensaje bloqueado por filtro de spam: " + motivo);
    }
}
