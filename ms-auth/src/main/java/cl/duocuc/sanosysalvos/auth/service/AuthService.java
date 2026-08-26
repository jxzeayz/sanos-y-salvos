package cl.duocuc.sanosysalvos.auth.service;

import cl.duocuc.sanosysalvos.auth.dto.AuthResponse;
import cl.duocuc.sanosysalvos.auth.dto.LoginRequest;
import cl.duocuc.sanosysalvos.auth.dto.RegisterRequest;
import cl.duocuc.sanosysalvos.auth.model.Usuario;
import cl.duocuc.sanosysalvos.auth.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import cl.duocuc.sanosysalvos.auth.model.RolUsuario;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final Set<RolUsuario> ROLES_REGISTRO = Set.of(RolUsuario.DUEÑO, RolUsuario.CIUDADANO, RolUsuario.VETERINARIO);

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya est\u00e1 registrado");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("La contrase\u00f1a debe tener al menos 8 caracteres");
        }

        if (!ROLES_REGISTRO.contains(request.getRol())) {
            throw new IllegalArgumentException("Rol no v\u00e1lido para registro");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .rol(request.getRol())
                .activo(true)
                .build();

        Usuario saved = usuarioRepository.save(usuario);
        return buildAuthResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailAndEliminadoFalse(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas"));

        if (!usuario.isActivo()) {
            throw new IllegalArgumentException("Usuario desactivado");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        return buildAuthResponse(usuario);
    }

    public String extractEmailFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token no proporcionado");
        }
        return jwtService.extractEmail(authHeader.substring(7));
    }

    public Map<String, Object> obtenerPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmailAndEliminadoFalse(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Map<String, Object> perfil = new HashMap<>();
        perfil.put("usuarioId", usuario.getId());
        perfil.put("nombre", usuario.getNombre());
        perfil.put("email", usuario.getEmail());
        perfil.put("telefono", usuario.getTelefono());
        perfil.put("rol", usuario.getRol());
        return perfil;
    }

    public Object actualizarPerfil(String email, Map<String, String> body) {
        Usuario usuario = usuarioRepository.findByEmailAndEliminadoFalse(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        boolean emailCambiado = false;

        if (body.containsKey("nombre")) usuario.setNombre(body.get("nombre"));
        if (body.containsKey("email")) {
            String nuevoEmail = body.get("email");
            if (!nuevoEmail.equals(usuario.getEmail()) && usuarioRepository.existsByEmail(nuevoEmail)) {
                throw new IllegalArgumentException("El email ya est\u00e1 en uso");
            }
            if (!nuevoEmail.equals(usuario.getEmail())) {
                emailCambiado = true;
            }
            usuario.setEmail(nuevoEmail);
        }
        if (body.containsKey("telefono")) usuario.setTelefono(body.get("telefono"));

        usuarioRepository.save(usuario);

        if (emailCambiado) {
            return buildAuthResponse(usuario);
        }

        return obtenerPerfil(usuario.getEmail());
    }

    public void cambiarPassword(String email, Map<String, String> body) {
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            throw new IllegalArgumentException("Debe proporcionar contrase\u00f1a actual y nueva");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("La nueva contrase\u00f1a debe tener al menos 8 caracteres");
        }

        Usuario usuario = usuarioRepository.findByEmailAndEliminadoFalse(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(currentPassword, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contrase\u00f1a actual es incorrecta");
        }

        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
    }

    public AuthResponse refreshToken(String token) {
        if (!jwtService.isTokenValid(token)) {
            throw new IllegalArgumentException("Token inv\u00e1lido o expirado");
        }
        Claims claims = jwtService.extractAllClaims(token);
        String email = claims.getSubject();
        Usuario usuario = usuarioRepository.findByEmailAndEliminadoFalse(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return buildAuthResponse(usuario);
    }

    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.isEliminado()) {
            throw new IllegalArgumentException("Usuario ya eliminado");
        }

        anonimizarDatos(usuario);
        usuario.setEliminado(true);
        usuario.setFechaEliminacion(LocalDateTime.now());
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Uso interno (llamado por otros microservicios dentro de la red Docker,
     * ej. ms-notificaciones para resolver el email de un usuario).
     */
    public Map<String, Object> buscarUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .filter(u -> !u.isEliminado())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Map<String, Object> info = new HashMap<>();
        info.put("id", usuario.getId());
        info.put("nombre", usuario.getNombre());
        info.put("email", usuario.getEmail());
        return info;
    }

    public List<Map<String, Object>> listarUsuarios() {
        return usuarioRepository.findByEliminadoFalse().stream()
                .map(u -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", u.getId());
                    info.put("nombre", u.getNombre());
                    info.put("email", u.getEmail());
                    info.put("telefono", u.getTelefono());
                    info.put("rol", u.getRol());
                    info.put("activo", u.isActivo());
                    info.put("createdAt", u.getCreatedAt());
                    return info;
                })
                .toList();
    }

    private void anonimizarDatos(Usuario usuario) {
        usuario.setEmail("anonimo" + usuario.getId() + "@eliminado");
        usuario.setNombre("Usuario Eliminado");
        usuario.setTelefono("0000000000");
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
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol())
                .build();
    }
}