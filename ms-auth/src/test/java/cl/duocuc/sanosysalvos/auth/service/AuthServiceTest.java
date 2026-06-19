package cl.duocuc.sanosysalvos.auth.service;

import cl.duocuc.sanosysalvos.auth.dto.AuthResponse;
import cl.duocuc.sanosysalvos.auth.dto.LoginRequest;
import cl.duocuc.sanosysalvos.auth.dto.RegisterRequest;
import cl.duocuc.sanosysalvos.auth.model.RolUsuario;
import cl.duocuc.sanosysalvos.auth.model.Usuario;
import cl.duocuc.sanosysalvos.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setNombre("Juan Pérez");
        registerRequest.setEmail("juan@test.cl");
        registerRequest.setPassword("password123");
        registerRequest.setTelefono("+56912345678");
        registerRequest.setRol(RolUsuario.DUEÑO);

        usuario = Usuario.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan@test.cl")
                .passwordHash("$2a$12$hash")
                .rol(RolUsuario.DUEÑO)
                .activo(true)
                .eliminado(false)
                .build();
    }

    @Test
    void register_exitoso_retornaTokenYDatos() {
        when(usuarioRepository.existsByEmail("juan@test.cl")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$12$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtService.generateToken(eq("juan@test.cl"), anyMap())).thenReturn("jwt.token.test");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.getToken()).isEqualTo("jwt.token.test");
        assertThat(response.getEmail()).isEqualTo("juan@test.cl");
        assertThat(response.getNombre()).isEqualTo("Juan Pérez");
        assertThat(response.getRol()).isEqualTo(RolUsuario.DUEÑO);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void register_emailDuplicado_lanzaIllegalArgumentException() {
        when(usuarioRepository.existsByEmail("juan@test.cl")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está registrado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void login_credencialesCorrectas_retornaToken() {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("juan@test.cl");
        loginReq.setPassword("password123");

        when(usuarioRepository.findByEmailAndEliminadoFalse("juan@test.cl")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "$2a$12$hash")).thenReturn(true);
        when(jwtService.generateToken(eq("juan@test.cl"), anyMap())).thenReturn("jwt.token.test");

        AuthResponse response = authService.login(loginReq);

        assertThat(response.getEmail()).isEqualTo("juan@test.cl");
        assertThat(response.getToken()).isEqualTo("jwt.token.test");
    }

    @Test
    void login_emailInexistente_lanzaExcepcion() {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("noexiste@test.cl");
        loginReq.setPassword("password123");

        when(usuarioRepository.findByEmailAndEliminadoFalse("noexiste@test.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciales incorrectas");
    }

    @Test
    void login_usuarioDesactivado_lanzaExcepcion() {
        usuario.setActivo(false);
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("juan@test.cl");
        loginReq.setPassword("password123");

        when(usuarioRepository.findByEmailAndEliminadoFalse("juan@test.cl")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authService.login(loginReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("desactivado");
    }

    @Test
    void login_passwordIncorrecto_lanzaExcepcion() {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("juan@test.cl");
        loginReq.setPassword("wrongPassword");

        when(usuarioRepository.findByEmailAndEliminadoFalse("juan@test.cl")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrongPassword", "$2a$12$hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciales incorrectas");
    }

    @Test
    void listarUsuarios_retornaListaNoVacia() {
        Usuario admin = Usuario.builder()
                .id(2L).nombre("Admin User").email("admin@test.cl")
                .rol(RolUsuario.ADMIN).activo(true).eliminado(false).build();

        when(usuarioRepository.findByEliminadoFalse()).thenReturn(List.of(usuario, admin));

        List<Map<String, Object>> resultado = authService.listarUsuarios();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).get("nombre")).isEqualTo("Juan Pérez");
        assertThat(resultado.get(1).get("rol")).isEqualTo(RolUsuario.ADMIN);
        verify(usuarioRepository).findByEliminadoFalse();
    }

    @Test
    void listarUsuarios_sinUsuarios_retornaListaVacia() {
        when(usuarioRepository.findByEliminadoFalse()).thenReturn(List.of());

        List<Map<String, Object>> resultado = authService.listarUsuarios();

        assertThat(resultado).isEmpty();
    }
}
