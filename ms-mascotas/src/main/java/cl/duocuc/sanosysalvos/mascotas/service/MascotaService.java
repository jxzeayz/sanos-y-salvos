package cl.duocuc.sanosysalvos.mascotas.service;

import cl.duocuc.sanosysalvos.mascotas.dto.MascotaRequest;
import cl.duocuc.sanosysalvos.mascotas.event.MascotaEventPublisher;
import cl.duocuc.sanosysalvos.mascotas.model.EstadoMascota;
import cl.duocuc.sanosysalvos.mascotas.model.Mascota;
import cl.duocuc.sanosysalvos.mascotas.repository.MascotaRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final MascotaEventPublisher eventPublisher;

    @Transactional
    public Mascota registrar(MascotaRequest req) {
        Mascota mascota = Mascota.builder()
                .nombre(req.getNombre())
                .especie(req.getEspecie())
                .raza(req.getRaza())
                .color(req.getColor())
                .descripcion(req.getDescripcion())
                .tamano(req.getTamano())
                .estado(req.getEstado())
                .latitud(req.getLatitud())
                .longitud(req.getLongitud())
                .usuarioId(req.getUsuarioId())
                .build();

        mascota = mascotaRepository.save(mascota);
        publishEventSafe(mascota);
        return mascota;
    }

    @CircuitBreaker(name = "matching-service", fallbackMethod = "publishFallback")
    private void publishEventSafe(Mascota mascota) {
        eventPublisher.publishMascotaRegistrada(mascota);
    }

    private void publishFallback(Mascota mascota, Exception ex) {
        log.warn("Circuit breaker activo: no se pudo publicar evento para mascota {}. Error: {}", mascota.getId(), ex.getMessage());
    }

    public List<Mascota> listarTodas() {
        return mascotaRepository.findAll();
    }

    public List<Mascota> listarPorEstado(EstadoMascota estado) {
        return mascotaRepository.findByEstado(estado);
    }

    public Mascota obtenerPorId(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada con id: " + id));
    }

    @Transactional
    public Mascota actualizarEstado(Long id, EstadoMascota nuevoEstado) {
        Mascota mascota = obtenerPorId(id);
        mascota.setEstado(nuevoEstado);
        return mascotaRepository.save(mascota);
    }

    public List<Mascota> listarPorUsuario(Long usuarioId) {
        return mascotaRepository.findByUsuarioId(usuarioId);
    }
}
