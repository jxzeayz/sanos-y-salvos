package com.sanosysalvos.msauditoria.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanosysalvos.msauditoria.model.AuditLog;
import com.sanosysalvos.msauditoria.service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
public class AuditEventConsumer {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public AuditEventConsumer(AuditService auditService, ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${rabbitmq.queue.auditoria}")
    public void procesarEvento(String mensaje) {
        java.util.Map<String, Object> evento;
        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> parsed = objectMapper.readValue(mensaje, java.util.Map.class);
            evento = parsed;
        } catch (Exception e) {
            throw new RuntimeException("Error deserializando evento de auditoria", e);
        }

        AuditLog auditLog = new AuditLog();
        auditLog.setEvento((String) evento.getOrDefault("evento", "desconocido"));
        auditLog.setServicioOrigen((String) evento.getOrDefault("servicioOrigen", "desconocido"));
        try {
            auditLog.setPayload(objectMapper.writeValueAsString(evento));
        } catch (Exception e) {
            throw new RuntimeException("Error serializando payload de auditoria", e);
        }
        auditLog.setFechaEvento(LocalDateTime.now());

        auditService.guardar(auditLog);
        log.info("Evento de auditoria registrado: {} desde {}", auditLog.getEvento(), auditLog.getServicioOrigen());
    }
}