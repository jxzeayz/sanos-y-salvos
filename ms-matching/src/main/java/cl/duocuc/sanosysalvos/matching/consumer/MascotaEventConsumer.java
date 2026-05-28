package cl.duocuc.sanosysalvos.matching.consumer;

import cl.duocuc.sanosysalvos.matching.model.MascotaSnapshot;
import cl.duocuc.sanosysalvos.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MascotaEventConsumer {

    private final MatchingService matchingService;

    @RabbitListener(queues = "${rabbitmq.queue.matching}")
    public void onMascotaEvent(Map<String, Object> evento) {
        log.info("Evento recibido para matching: {}", evento);

        try {
            MascotaSnapshot snapshot = MascotaSnapshot.builder()
                    .mascotaId(toLong(evento.get("mascotaId")))
                    .especie((String) evento.get("especie"))
                    .raza((String) evento.get("raza"))
                    .color((String) evento.get("color"))
                    .tamano((String) evento.get("tamano"))
                    .estado((String) evento.get("estado"))
                    .latitud(toDouble(evento.get("latitud")))
                    .longitud(toDouble(evento.get("longitud")))
                    .fechaReporte(LocalDateTime.parse((String) evento.get("fechaReporte")))
                    .build();

            matchingService.procesarNuevaMascota(snapshot);
        } catch (Exception e) {
            log.error("Error procesando evento de mascota: {}", e.getMessage());
        }
    }

    private Long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }

    private Double toDouble(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.doubleValue();
        return Double.parseDouble(val.toString());
    }
}
