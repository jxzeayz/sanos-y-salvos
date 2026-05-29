package cl.duocuc.sanosysalvos.geolocalizacion.service;

import cl.duocuc.sanosysalvos.geolocalizacion.dto.ZonaReporteRequest;
import cl.duocuc.sanosysalvos.geolocalizacion.model.ZonaReporte;
import cl.duocuc.sanosysalvos.geolocalizacion.repository.ZonaReporteRepository;
import cl.duocuc.sanosysalvos.geolocalizacion.dto.ZonaReporteMapaResponse;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeoService {

    private final ZonaReporteRepository zonaReporteRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public ZonaReporte registrarZona(ZonaReporteRequest req) {
        Point punto = geometryFactory.createPoint(new Coordinate(req.getLongitud(), req.getLatitud()));

        ZonaReporte zona = ZonaReporte.builder()
                .mascotaId(req.getMascotaId())
                .usuarioId(req.getUsuarioId())
                .latitud(req.getLatitud())
                .longitud(req.getLongitud())
                .ubicacion(punto)
                .tipoReporte(req.getTipoReporte())
                .descripcion(req.getDescripcion())
                .build();

        return zonaReporteRepository.save(zona);
    }

    public List<ZonaReporteMapaResponse> listarParaMapa() {
        return zonaReporteRepository.findAll()
                .stream()
                .map(zona -> ZonaReporteMapaResponse.builder()
                        .id(zona.getId())
                        .mascotaId(zona.getMascotaId())
                        .usuarioId(zona.getUsuarioId())
                        .latitud(zona.getLatitud())
                        .longitud(zona.getLongitud())
                        .tipoReporte(zona.getTipoReporte())
                        .estado(zona.getTipoReporte())
                        .descripcion(zona.getDescripcion())
                        .fechaReporte(zona.getFechaReporte())
                        .build())
                .toList();
    }

    public List<ZonaReporte> buscarEnRadio(double latitud, double longitud, double radioMetros) {
        return zonaReporteRepository.findByRadio(latitud, longitud, radioMetros);
    }

    public List<ZonaReporte> listarTodas() {
        return zonaReporteRepository.findAll();
    }

    public List<Map<String, Object>> obtenerZonasCalientes() {
        return zonaReporteRepository.findZonasCalientes().stream()
                .map(row -> Map.<String, Object>of(
                        "latitud", row[0],
                        "longitud", row[1],
                        "cantidad", row[2]))
                .collect(Collectors.toList());
    }
}
