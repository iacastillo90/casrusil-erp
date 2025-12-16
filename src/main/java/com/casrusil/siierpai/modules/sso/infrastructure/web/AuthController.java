package com.casrusil.siierpai.modules.sso.infrastructure.web;

import com.casrusil.siierpai.modules.sso.application.dto.AuthResult;
import com.casrusil.siierpai.modules.sso.application.service.AuthService;
import com.casrusil.siierpai.modules.sso.infrastructure.web.dto.AuthResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para autenticación pública.
 * 
 * <p>
 * Maneja el registro de nuevas empresas y el inicio de sesión de usuarios.
 * Estos endpoints son públicos (no requieren token JWT).
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 * <li>{@code POST /api/v1/auth/register}: Registrar nueva empresa.</li>
 * <li>{@code POST /api/v1/auth/login}: Iniciar sesión y obtener token.</li>
 * </ul>
 * 
 * @see AuthService
 * @since 1.0
 */
@RestController
@RequestMapping({ "/api/v1/auth", "/auth" })
public class AuthController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        log.info("🚀 CARGANDO AUTH CONTROLLER");
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResult result = authService.registerCompany(
                request.rut(),
                request.razonSocial(),
                request.adminEmail(),
                request.adminPassword());
        return ResponseEntity.ok(mapToResponse(result));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResult result = authService.login(request.email(), request.password());
        return ResponseEntity.ok(mapToResponse(result));
    }

    private AuthResponse mapToResponse(AuthResult result) {
        return new AuthResponse(
                result.token(),
                result.user().getId().getValue(),
                result.user().getEmail(), // Usemos email como nombre por ahora si nombre no está disponible
                result.company().getId().getValue(),
                result.company().getRazonSocial());
    }

    public record RegisterRequest(
            @NotBlank String rut,
            @NotBlank String razonSocial,
            @NotBlank @Email String adminEmail,
            @NotBlank String adminPassword) {
    }

    public record LoginRequest(String email, String password) {
    }
}
