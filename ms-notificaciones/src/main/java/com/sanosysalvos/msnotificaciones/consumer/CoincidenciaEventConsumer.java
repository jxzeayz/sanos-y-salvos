package com.sanosysalvos.msnotificaciones.consumer;

import com.sanosysalvos.msnotificaciones.model.Notificacion;
import com.sanosysalvos.msnotificaciones.model.TipoNotificacion;
import com.sanosysalvos.msnotificaciones.service.NotificacionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class CoincidenciaEventConsumer {

    private final NotificacionService notificacionService;

    public CoincidenciaEventConsumer(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.notificaciones}")
    public void procesarCoincidencia(Map<String, Object> evento) {
        Object usuarioIdObj = evento.get("usuarioId");
        if (usuarioIdObj == null) {
            log.warn("Evento sin usuarioId, ignorando: {}", evento);
            throw new IllegalArgumentException("Evento sin usuarioId requerido");
        }
        Long usuarioId;
        if (usuarioIdObj instanceof Number) {
            usuarioId = ((Number) usuarioIdObj).longValue();
        } else {
            usuarioId = Long.parseLong(usuarioIdObj.toString());
        }

        Double score = null;
        Object scoreObj = evento.get("scoreMatch");
        if (scoreObj instanceof Number) {
            score = ((Number) scoreObj).doubleValue();
        }

        String mensajeNotificacion = evento.get("mensaje") != null
                ? evento.get("mensaje").toString()
                : "Se ha encontrado una mascota que coincide con tu reporte";

        if (score != null) {
            mensajeNotificacion += " (compatibilidad: " + String.format("%.0f", score * 100) + "%)";
        }

        Long usuarioIdEncontrada = null;
        Object userIdEncObj = evento.get("usuarioIdEncontrada");
        if (userIdEncObj instanceof Number) {
            usuarioIdEncontrada = ((Number) userIdEncObj).longValue();
        } else if (userIdEncObj != null) {
            usuarioIdEncontrada = Long.parseLong(userIdEncObj.toString());
        }

        Notificacion notificacion = new Notificacion();
        notificacion.setTipo(TipoNotificacion.Tipo.COINCIDENCIA);
        notificacion.setTitulo("Coincidencia detectada");
        notificacion.setMensaje(mensajeNotificacion);
        notificacion.setUsuarioId(usuarioId);
        notificacion.setOrigenUsuarioId(usuarioIdEncontrada);
        notificacion.setEnviadaEn(LocalDateTime.now());

        notificacionService.guardar(notificacion);
        log.info("Notificacion guardada para usuario {}", usuarioId);
    }
}
