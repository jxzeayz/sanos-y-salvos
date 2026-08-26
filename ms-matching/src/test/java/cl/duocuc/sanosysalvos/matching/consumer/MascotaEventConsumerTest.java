package cl.duocuc.sanosysalvos.matching.consumer;

import cl.duocuc.sanosysalvos.matching.model.MascotaSnapshot;
import cl.duocuc.sanosysalvos.matching.service.MatchingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MascotaEventConsumerTest {

    @Mock private MatchingService matchingService;

    @InjectMocks
    private MascotaEventConsumer consumer;

    @Test
    void onMascotaEvent_eventoEliminada_eliminaSnapshotYNoProcesaMatching() {
        Map<String, Object> evento = Map.of(
                "evento", "mascota.eliminada",
                "mascotaId", 5,
                "usuarioId", 1
        );

        consumer.onMascotaEvent(evento);

        verify(matchingService).eliminarMascota(5L);
        verify(matchingService, never()).procesarNuevaMascota(any());
    }

    @Test
    void onMascotaEvent_eventoRegistrada_procesaComoUpsert() {
        Map<String, Object> evento = Map.of(
                "evento", "mascota.registrada",
                "mascotaId", 5,
                "usuarioId", 1,
                "nombre", "Firulais",
                "especie", "PERRO",
                "estado", "PERDIDA",
                "latitud", -33.45,
                "longitud", -70.64,
                "fechaReporte", "2026-01-01T10:00:00"
        );

        consumer.onMascotaEvent(evento);

        ArgumentCaptor<MascotaSnapshot> captor = ArgumentCaptor.forClass(MascotaSnapshot.class);
        verify(matchingService).procesarNuevaMascota(captor.capture());
        verify(matchingService, never()).eliminarMascota(anyLong());
        assertThat(captor.getValue().getMascotaId()).isEqualTo(5L);
    }
}
