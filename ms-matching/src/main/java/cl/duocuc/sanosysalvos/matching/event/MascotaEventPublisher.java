package cl.duocuc.sanosysalvos.matching.event;

import cl.duocuc.sanosysalvos.matching.model.Coincidencia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MascotaEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    public void publishCoincidenciaHallada(Coincidencia coincidencia) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("coincidenciaId", coincidencia.getId());
        evento.put("mascotaPerdidaId", coincidencia.getMascotaPerdidaId());
        evento.put("mascotaEncontradaId", coincidencia.getMascotaEncontradaId());
        evento.put("usuarioId", coincidencia.getUsuarioIdPerdida());
        evento.put("usuarioIdEncontrada", coincidencia.getUsuarioIdEncontrada());
        evento.put("scoreMatch", coincidencia.getScoreMatch());
        evento.put("estado", coincidencia.getEstado().name());
        evento.put("fechaDeteccion", coincidencia.getFechaDeteccion().toString());
        evento.put("mensaje", "Se encontró una mascota que coincide con tu reporte de mascota perdida");

        rabbitTemplate.convertAndSend(exchange, "coincidencia.hallada", evento);
        log.info("Evento publicado: coincidencia.hallada para coincidencia ID {}", coincidencia.getId());
    }
}