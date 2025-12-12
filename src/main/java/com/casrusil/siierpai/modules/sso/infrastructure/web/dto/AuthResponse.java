package com.casrusil.siierpai.modules.sso.infrastructure.web.dto;

import java.util.UUID;

/**
 * DTO de respuesta para autenticación exitosa (Login/Register).
 * 
 * <p>
 * Devuelve no solo el token JWT, sino también el contexto mínimo necesario
 * para que el frontend pueda inicializar su estado (Store) sin hacer una
 * petición adicional a /me.
 * 
 * @param token       JWT Bearer token
 * @param userId      ID del usuario autenticado
 * @param userName    Nombre del usuario (o email si no tiene nombre)
 * @param companyId   ID de la empresa (contexto actual)
 * @param companyName Nombre de la empresa
 */
public record AuthResponse(
        String token,
        UUID userId,
        String userName,
        UUID companyId,
        String companyName) {
}
