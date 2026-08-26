package com.sanosysalvos.msnotificaciones.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envío "best effort": si falla (SMTP no configurado, credenciales de prueba, etc.)
     * se loggea y no se relanza, para no perder la notificación ya persistida en BD.
     */
    public void enviarNotificacion(String destinatario, String asunto, String mensaje) {
        if (destinatario == null || destinatario.isBlank()) {
            log.warn("No se pudo enviar el correo '{}': destinatario desconocido", asunto);
            return;
        }
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(remitente);
            email.setTo(destinatario);
            email.setSubject(asunto);
            email.setText(mensaje);
            mailSender.send(email);
            log.info("Correo enviado a {} ({})", destinatario, asunto);
        } catch (Exception e) {
            log.warn("Fallo el envío de correo a {}: {}", destinatario, e.getMessage());
        }
    }
}
