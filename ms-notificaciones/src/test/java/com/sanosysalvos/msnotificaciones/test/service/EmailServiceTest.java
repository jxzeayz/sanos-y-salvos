package com.sanosysalvos.msnotificaciones.test.service;

import com.sanosysalvos.msnotificaciones.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "remitente", "notificaciones@sanosysalvos.com");
    }

    @Test
    void enviarNotificacion_destinatarioValido_enviaCorreo() {
        emailService.enviarNotificacion("usuario@correo.com", "Asunto", "Mensaje");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void enviarNotificacion_destinatarioNulo_noIntentaEnviar() {
        emailService.enviarNotificacion(null, "Asunto", "Mensaje");

        verifyNoInteractions(mailSender);
    }

    @Test
    void enviarNotificacion_fallaEnvio_noLanzaExcepcion() {
        doThrow(new RuntimeException("SMTP no disponible")).when(mailSender).send(any(SimpleMailMessage.class));

        emailService.enviarNotificacion("usuario@correo.com", "Asunto", "Mensaje");
    }
}
