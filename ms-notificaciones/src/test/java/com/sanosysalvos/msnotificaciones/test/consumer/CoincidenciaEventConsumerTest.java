package com.sanosysalvos.msnotificaciones.test.consumer;

import com.sanosysalvos.msnotificaciones.client.UsuarioClient;
import com.sanosysalvos.msnotificaciones.consumer.CoincidenciaEventConsumer;
import com.sanosysalvos.msnotificaciones.model.Notificacion;
import com.sanosysalvos.msnotificaciones.service.EmailService;
import com.sanosysalvos.msnotificaciones.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoincidenciaEventConsumerTest {

    @Mock private NotificacionService notificacionService;
    @Mock private UsuarioClient usuarioClient;
    @Mock private EmailService emailService;

    @InjectMocks
    private CoincidenciaEventConsumer consumer;

    @Test
    void procesarCoincidencia_guardaNotificacionYEnviaCorreo() {
        Map<String, Object> evento = Map.of(
                "usuarioId", 1,
                "scoreMatch", 0.85,
                "mensaje", "Coincidencia"
        );
        when(usuarioClient.obtenerEmail(1L)).thenReturn("dueno@correo.com");

        consumer.procesarCoincidencia(evento);

        verify(notificacionService).guardar(any(Notificacion.class));
        verify(emailService).enviarNotificacion(eq("dueno@correo.com"), anyString(), anyString());
    }

    @Test
    void procesarCoincidencia_fallaResolucionDeEmail_igualGuardaNotificacion() {
        Map<String, Object> evento = Map.of("usuarioId", 1, "mensaje", "Coincidencia");
        when(usuarioClient.obtenerEmail(1L)).thenReturn(null);

        consumer.procesarCoincidencia(evento);

        verify(notificacionService).guardar(any(Notificacion.class));
        verify(emailService).enviarNotificacion(isNull(), anyString(), anyString());
    }

    @Test
    void procesarCoincidencia_sinUsuarioId_lanzaIllegalArgumentException() {
        Map<String, Object> evento = Map.of("mensaje", "Coincidencia");

        try {
            consumer.procesarCoincidencia(evento);
            throw new AssertionError("Debió lanzar IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // ok
        }

        verifyNoInteractions(notificacionService, emailService);
    }
}
