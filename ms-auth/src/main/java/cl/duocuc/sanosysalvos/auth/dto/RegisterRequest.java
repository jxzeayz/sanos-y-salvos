package cl.duocuc.sanosysalvos.auth.dto;

import cl.duocuc.sanosysalvos.auth.model.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String telefono;

    @NotNull
    private RolUsuario rol;
}
