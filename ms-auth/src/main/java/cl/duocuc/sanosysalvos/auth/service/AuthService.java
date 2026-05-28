package cl.duocuc.sanosysalvos.auth.service;

import cl.duocuc.sanosysalvos.auth.dto.AuthResponse;
import cl.duocuc.sanosysalvos.auth.dto.LoginRequest;
import cl.duocuc.sanosysalvos.auth.dto.RegisterRequest;
import cl.duocuc.sanosysalvos.auth.model.Usuario;
import cl.duocuc.sanosysalvos.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .rol(request.getRol())
                .activo(true)
                .build();

        usuarioRepository.save(usuario);
        return buildAuthResponse(usuario);
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas"));

        if (!usuario.isActivo()) {
            throw new IllegalArgumentException("Usuario desactivado");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        return buildAuthResponse(usuario);
    }

    private AuthResponse buildAuthResponse(Usuario usuario) {
        Map<String, Object> claims = Map.of(
                "usuarioId", usuario.getId(),
                "rol", usuario.getRol().name()
        );

        String token = jwtService.generateToken(usuario.getEmail(), claims);

        return AuthResponse.builder()
                .token(token)
                .usuarioId(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }
}
