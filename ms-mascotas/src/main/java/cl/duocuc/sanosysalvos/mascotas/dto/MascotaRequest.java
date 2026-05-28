package cl.duocuc.sanosysalvos.mascotas.dto;

import cl.duocuc.sanosysalvos.mascotas.model.EstadoMascota;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MascotaRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    private String especie;

    private String raza;

    @NotBlank
    private String color;

    private String descripcion;
    private String tamano;

    @NotNull
    private EstadoMascota estado;

    private Double latitud;
    private Double longitud;

    @NotNull
    private Long usuarioId;
}
