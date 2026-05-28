package cl.duocuc.sanosysalvos.auth.dto;

import cl.duocuc.sanosysalvos.auth.model.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long usuarioId;
    private String nombre;
    private String email;
    private RolUsuario rol;
}
