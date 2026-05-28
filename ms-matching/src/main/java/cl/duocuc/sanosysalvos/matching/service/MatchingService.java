package cl.duocuc.sanosysalvos.matching.service;

import cl.duocuc.sanosysalvos.matching.model.Coincidencia;
import cl.duocuc.sanosysalvos.matching.model.EstadoCoincidencia;
import cl.duocuc.sanosysalvos.matching.model.MascotaSnapshot;
import cl.duocuc.sanosysalvos.matching.repository.CoincidenciaRepository;
import cl.duocuc.sanosysalvos.matching.repository.MascotaSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MascotaSnapshotRepository snapshotRepository;
    private final CoincidenciaRepository coincidenciaRepository;
    private final MatchingAlgorithm algorithm;

    @Value("${matching.score-minimo:0.60}")
    private double scoreMinimo;

    @Transactional
    public List<Coincidencia> procesarNuevaMascota(MascotaSnapshot nueva) {
        snapshotRepository.save(nueva);

        String estadoOpuesto = nueva.getEstado().equals("PERDIDA") ? "ENCONTRADA" : "PERDIDA";
        List<MascotaSnapshot> candidatas = snapshotRepository
                .findByEspecieAndEstado(nueva.getEspecie(), estadoOpuesto);

        List<Coincidencia> coincidencias = new ArrayList<>();

        for (MascotaSnapshot candidata : candidatas) {
            MascotaSnapshot perdida    = nueva.getEstado().equals("PERDIDA") ? nueva : candidata;
            MascotaSnapshot encontrada = nueva.getEstado().equals("PERDIDA") ? candidata : nueva;

            if (coincidenciaRepository.existsByMascotaPerdidaIdAndMascotaEncontradaId(
                    perdida.getMascotaId(), encontrada.getMascotaId())) {
                continue;
            }

            double score = algorithm.calcularScore(perdida, encontrada);
            log.info("Score entre mascota {} y {}: {}", perdida.getMascotaId(), encontrada.getMascotaId(), score);

            if (score >= scoreMinimo) {
                Coincidencia c = Coincidencia.builder()
                        .mascotaPerdidaId(perdida.getMascotaId())
                        .mascotaEncontradaId(encontrada.getMascotaId())
                        .scoreMatch(score)
                        .estado(EstadoCoincidencia.PENDIENTE)
                        .build();
                coincidencias.add(coincidenciaRepository.save(c));
            }
        }

        log.info("Matching completado para mascota {}: {} coincidencias encontradas", nueva.getMascotaId(), coincidencias.size());
        return coincidencias;
    }

    public List<Coincidencia> listarCoincidencias() {
        return coincidenciaRepository.findAll();
    }

    public List<Coincidencia> listarPorMascota(Long mascotaId) {
        return coincidenciaRepository.findByMascotaPerdidaId(mascotaId);
    }

    @Transactional
    public Coincidencia actualizarEstado(Long id, EstadoCoincidencia estado) {
        Coincidencia c = coincidenciaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coincidencia no encontrada: " + id));
        c.setEstado(estado);
        return coincidenciaRepository.save(c);
    }
}
