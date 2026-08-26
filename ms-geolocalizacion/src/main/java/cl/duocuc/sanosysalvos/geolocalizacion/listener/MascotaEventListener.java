package cl.duocuc.sanosysalvos.geolocalizacion.listener;

import cl.duocuc.sanosysalvos.geolocalizacion.config.RabbitMQConfig;
import cl.duocuc.sanosysalvos.geolocalizacion.dto.ZonaReporteRequest;
import cl.duocuc.sanosysalvos.geolocalizacion.service.GeoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MascotaEventListener {

    private final GeoService geoService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_GEO)
    public void procesarMascotaRegistrada(Map<String, Object> evento) {
        log.info("Evento recibido en MS-Geolocalización: {}", evento);

        Long mascotaId = toLong(evento.get("mascotaId"));

        String tipoEvento = (String) evento.get("evento");
        boolean esEliminacion = "mascota.eliminada".equals(tipoEvento);
        boolean esReunificacion = "REUNIFICADA".equals(evento.get("estado"));
        if (esEliminacion || esReunificacion) {
            if (mascotaId == null) {
                log.warn("Evento ignorado (eliminación/reunificación) sin mascotaId: {}", evento);
                return;
            }
            geoService.eliminarZonasDeMascota(mascotaId);
            log.info("Zonas de reporte eliminadas para mascota ID {} (eliminada={}, reunificada={})",
                    mascotaId, esEliminacion, esReunificacion);
            return;
        }

        Long usuarioId = toLong(evento.get("usuarioId"));
        Double latitud = toDouble(evento.get("latitud"));
        Double longitud = toDouble(evento.get("longitud"));

        String tipoReporte = evento.get("tipoReporte") != null
                ? String.valueOf(evento.get("tipoReporte"))
                : String.valueOf(evento.get("estado"));

        if ("null".equals(tipoReporte) || tipoReporte == null) {
            log.warn("Evento ignorado porque tipoReporte/estado no proporcionado: {}", evento);
            return;
        }

        if (mascotaId == null || usuarioId == null || latitud == null || longitud == null) {
            log.warn("Evento ignorado porque faltan datos obligatorios para geolocalización: {}", evento);
            return;
        }

        ZonaReporteRequest request = new ZonaReporteRequest();
        request.setMascotaId(mascotaId);
        request.setUsuarioId(usuarioId);
        request.setLatitud(latitud);
        request.setLongitud(longitud);
        request.setTipoReporte(tipoReporte);
        request.setDescripcion((String) evento.get("descripcion"));

        geoService.registrarZona(request);

        log.info("Zona de reporte creada desde evento para mascota ID {}", request.getMascotaId());
    }

    private Long toLong(Object value) {
        if (value == null)
            return null;
        if (value instanceof Number number)
            return number.longValue();
        return Long.valueOf(value.toString());
    }

    private Double toDouble(Object value) {
        if (value == null)
            return null;
        if (value instanceof Number number)
            return number.doubleValue();
        return Double.valueOf(value.toString());
    }
}
