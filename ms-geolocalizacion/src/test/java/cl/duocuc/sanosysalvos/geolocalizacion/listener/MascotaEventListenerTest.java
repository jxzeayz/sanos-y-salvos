package cl.duocuc.sanosysalvos.geolocalizacion.listener;

import cl.duocuc.sanosysalvos.geolocalizacion.dto.ZonaReporteRequest;
import cl.duocuc.sanosysalvos.geolocalizacion.service.GeoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MascotaEventListenerTest {

    @Mock private GeoService geoService;

    @InjectMocks
    private MascotaEventListener listener;

    @Test
    void procesarMascotaRegistrada_eventoEliminada_borraZonasDeLaMascota() {
        Map<String, Object> evento = Map.of(
                "evento", "mascota.eliminada",
                "mascotaId", 7,
                "usuarioId", 1,
                "estado", "PERDIDA"
        );

        listener.procesarMascotaRegistrada(evento);

        verify(geoService).eliminarZonasDeMascota(7L);
        verify(geoService, never()).registrarZona(any());
    }

    @Test
    void procesarMascotaRegistrada_estadoReunificada_borraZonasDeLaMascota() {
        Map<String, Object> evento = Map.of(
                "evento", "mascota.estado_actualizado",
                "mascotaId", 7,
                "usuarioId", 1,
                "estado", "REUNIFICADA"
        );

        listener.procesarMascotaRegistrada(evento);

        verify(geoService).eliminarZonasDeMascota(7L);
        verify(geoService, never()).registrarZona(any());
    }

    @Test
    void procesarMascotaRegistrada_eventoPerdida_registraZona() {
        Map<String, Object> evento = Map.of(
                "evento", "mascota.registrada",
                "mascotaId", 7L,
                "usuarioId", 1L,
                "estado", "PERDIDA",
                "latitud", -33.45,
                "longitud", -70.64
        );

        listener.procesarMascotaRegistrada(evento);

        verify(geoService).registrarZona(any(ZonaReporteRequest.class));
        verify(geoService, never()).eliminarZonasDeMascota(any());
    }
}
