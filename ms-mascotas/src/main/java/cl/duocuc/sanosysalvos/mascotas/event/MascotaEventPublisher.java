package cl.duocuc.sanosysalvos.mascotas.event;

import cl.duocuc.sanosysalvos.mascotas.model.Mascota;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

    @CircuitBreaker(name = "matching-service", fallbackMethod = "publishFallback")
    public void publishMascotaRegistrada(Mascota mascota) {
        Map<String, Object> evento = buildEventoBase(mascota);
        evento.put("evento", "mascota.registrada");
        String routingKey = "mascota." + (mascota.getEstado() != null ? mascota.getEstado().name().toLowerCase() : "registrada");
        rabbitTemplate.convertAndSend(exchange, routingKey, evento);
        log.info("Evento publicado: {} para mascota ID {}", routingKey, mascota.getId());
    }

    @CircuitBreaker(name = "matching-service", fallbackMethod = "publishFallback")
    public void publishMascotaActualizada(Mascota mascota) {
        Map<String, Object> evento = buildEventoBase(mascota);
        evento.put("evento", "mascota.actualizada");
        String routingKey = "mascota." + (mascota.getEstado() != null ? mascota.getEstado().name().toLowerCase() : "actualizada");
        rabbitTemplate.convertAndSend(exchange, routingKey, evento);
        log.info("Evento publicado: {} para mascota ID {}", routingKey, mascota.getId());
    }

    public void publishMascotaEstadoActualizado(Mascota mascota) {
        Map<String, Object> evento = buildEventoBase(mascota);
        evento.put("evento", "mascota.estado_actualizado");
        String routingKey = "mascota." + mascota.getEstado().name().toLowerCase();
        rabbitTemplate.convertAndSend(exchange, routingKey, evento);
        log.info("Evento publicado: {} para mascota ID {}", routingKey, mascota.getId());
    }

    @CircuitBreaker(name = "matching-service", fallbackMethod = "publishFallback")
    public void publishMascotaEliminada(Mascota mascota) {
        Map<String, Object> evento = buildEventoBase(mascota);
        evento.put("evento", "mascota.eliminada");
        rabbitTemplate.convertAndSend(exchange, "mascota.eliminada", evento);
        log.info("Evento publicado: mascota.eliminada para mascota ID {}", mascota.getId());
    }

    private void publishFallback(Mascota mascota, Exception ex) {
        log.warn("Circuit breaker activo: no se pudo publicar evento para mascota {}. Error: {}", mascota.getId(), ex.getMessage());
    }

    private Map<String, Object> buildEventoBase(Mascota mascota) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("mascotaId", mascota.getId());
        evento.put("usuarioId", mascota.getUsuarioId());
        evento.put("nombre", mascota.getNombre());
        evento.put("especie", mascota.getEspecie() != null ? mascota.getEspecie().name() : null);
        evento.put("raza", mascota.getRaza());
        evento.put("color", mascota.getColor());
        evento.put("tamano", mascota.getTamano() != null ? mascota.getTamano().name() : null);
        evento.put("estado", mascota.getEstado() != null ? mascota.getEstado().name() : null);
        evento.put("tipoReporte", mascota.getEstado() != null ? mascota.getEstado().name() : null);
        evento.put("descripcion", mascota.getDescripcion());
        evento.put("latitud", mascota.getLatitud());
        evento.put("longitud", mascota.getLongitud());
        evento.put("fechaReporte", mascota.getFechaReporte() != null ? mascota.getFechaReporte().toString() : null);
        return evento;
    }
}
