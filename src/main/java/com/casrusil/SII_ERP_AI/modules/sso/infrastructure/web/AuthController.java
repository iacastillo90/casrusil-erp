package com.casrusil.SII_ERP_AI.modules.sso.infrastructure.web;

import com.casrusil.SII_ERP_AI.modules.sso.application.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        String token = authService.registerCompany(
                request.rut(),
                request.razonSocial(),
                request.adminEmail(),
                request.adminPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(Map.of("token", token));
    }

    public record RegisterRequest(String rut, String razonSocial, String adminEmail, String adminPassword) {
    }

    public record LoginRequest(String email, String password) {
    }
}
