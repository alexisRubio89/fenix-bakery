package com.bakery.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private FiltroPalabrasService filtro;

    @Value("${bakery.contact.recipient}")
    private String recipient;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void enviarMensajeContacto(String nombre, String email, String telefono,
                                      String asunto, String mensaje) {
        // Censura el contenido de texto libre antes de enviar
        String nombreLimpio = filtro.censurar(nombre);
        String asuntoLimpio = filtro.censurar(asunto);
        String mensajeLimpio = filtro.censurar(mensaje);

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(recipient);
        mail.setFrom(fromAddress);
        mail.setReplyTo(email);
        mail.setSubject("Nuevo mensaje de contacto: " + asuntoLimpio);

        String cuerpo = "Has recibido un nuevo mensaje desde el formulario de Fenix Bakery.\n\n"
                + "Nombre:   " + nombreLimpio + "\n"
                + "Email:    " + email + "\n"
                + "Teléfono: " + (telefono == null || telefono.isBlank() ? "(no proporcionado)" : telefono) + "\n"
                + "Asunto:   " + asuntoLimpio + "\n\n"
                + "Mensaje:\n" + mensajeLimpio + "\n";

        mail.setText(cuerpo);
        mailSender.send(mail);
    }
}
