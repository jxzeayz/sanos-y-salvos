package cl.duocuc.sanosysalvos.mascotas.event;

import cl.duocuc.sanosysalvos.mascotas.model.Mascota;
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

    public void publishMascotaRegistrada(Mascota mascota) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("mascotaId", mascota.getId());
        evento.put("especie", mascota.getEspecie());
        evento.put("raza", mascota.getRaza());
        evento.put("color", mascota.getColor());
        evento.put("tamano", mascota.getTamano());
        evento.put("estado", mascota.getEstado().name());
        evento.put("latitud", mascota.getLatitud());
        evento.put("longitud", mascota.getLongitud());
        evento.put("fechaReporte", mascota.getFechaReporte().toString());

        String routingKey = "mascota." + mascota.getEstado().name().toLowerCase();
        rabbitTemplate.convertAndSend(exchange, routingKey, evento);
        log.info("Evento publicado: {} para mascota ID {}", routingKey, mascota.getId());
    }
}
