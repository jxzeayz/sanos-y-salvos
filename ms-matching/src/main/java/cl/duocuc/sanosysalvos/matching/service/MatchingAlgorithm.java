package cl.duocuc.sanosysalvos.matching.service;

import cl.duocuc.sanosysalvos.matching.model.MascotaSnapshot;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

/**
 * Algoritmo de scoring ponderado entre dos mascotas.
 * Score máximo: 1.0
 *
 * Pesos:
 *   especie   → 0.30  (excluyente si no coincide)
 *   raza      → 0.25
 *   color     → 0.20
 *   tamaño    → 0.10
 *   ubicacion → 0.10
 *   fecha     → 0.05
 */
@Component
public class MatchingAlgorithm {

    private static final double RADIO_MAXIMO_KM = 10.0;
    private static final long DIAS_MAXIMOS = 30;

    public double calcularScore(MascotaSnapshot perdida, MascotaSnapshot encontrada) {
        if (!mismaEspecie(perdida, encontrada)) {
            return 0.0;
        }

        double score = 0.30;
        score += scoreRaza(perdida, encontrada)    * 0.25;
        score += scoreColor(perdida, encontrada)   * 0.20;
        score += scoreTamano(perdida, encontrada)  * 0.10;
        score += scoreUbicacion(perdida, encontrada) * 0.10;
        score += scoreFecha(perdida, encontrada)   * 0.05;

        return Math.min(score, 1.0);
    }

    private boolean mismaEspecie(MascotaSnapshot a, MascotaSnapshot b) {
        if (a.getEspecie() == null || b.getEspecie() == null) return false;
        return a.getEspecie().equalsIgnoreCase(b.getEspecie());
    }

    private double scoreRaza(MascotaSnapshot a, MascotaSnapshot b) {
        if (a.getRaza() == null || b.getRaza() == null) return 0.5;
        return a.getRaza().equalsIgnoreCase(b.getRaza()) ? 1.0 : 0.0;
    }

    private double scoreColor(MascotaSnapshot a, MascotaSnapshot b) {
        if (a.getColor() == null || b.getColor() == null) return 0.0;
        String colorA = a.getColor().toLowerCase();
        String colorB = b.getColor().toLowerCase();
        if (colorA.equals(colorB)) return 1.0;
        if (colorA.contains(colorB) || colorB.contains(colorA)) return 0.6;
        return 0.0;
    }

    private double scoreTamano(MascotaSnapshot a, MascotaSnapshot b) {
        if (a.getTamano() == null || b.getTamano() == null) return 0.5;
        return a.getTamano().equalsIgnoreCase(b.getTamano()) ? 1.0 : 0.0;
    }

    private double scoreUbicacion(MascotaSnapshot a, MascotaSnapshot b) {
        if (a.getLatitud() == null || b.getLatitud() == null) return 0.0;
        double distanciaKm = haversineKm(a.getLatitud(), a.getLongitud(), b.getLatitud(), b.getLongitud());
        if (distanciaKm > RADIO_MAXIMO_KM) return 0.0;
        return 1.0 - (distanciaKm / RADIO_MAXIMO_KM);
    }

    private double scoreFecha(MascotaSnapshot a, MascotaSnapshot b) {
        if (a.getFechaReporte() == null || b.getFechaReporte() == null) return 0.0;
        long dias = Math.abs(ChronoUnit.DAYS.between(a.getFechaReporte(), b.getFechaReporte()));
        if (dias > DIAS_MAXIMOS) return 0.0;
        return 1.0 - ((double) dias / DIAS_MAXIMOS);
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
