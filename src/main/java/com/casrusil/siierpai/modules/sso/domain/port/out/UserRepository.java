package com.casrusil.siierpai.modules.sso.domain.port.out;

import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;

import java.util.Optional;

/**
 * Repositorio para persistencia de usuarios.
 * 
 * <p>
 * Define el contrato de persistencia para entidades {@link User} siguiendo
 * el patrón Repository de DDD. Cada usuario pertenece a una empresa específica
 * (multi-tenancy).
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Persistir y recuperar usuarios</li>
 * <li>Validar unicidad de email</li>
 * <li>Buscar usuarios para autenticación</li>
 * </ul>
 * 
 * <h2>Seguridad:</h2>
 * <ul>
 * <li>Las contraseñas se almacenan hasheadas (BCrypt)</li>
 * <li>Los emails son únicos a nivel global</li>
 * </ul>
 * 
 * @see User
 * @see com.casrusil.siierpai.modules.sso.infrastructure.adapter.out.persistence.UserJpaAdapter
 * @since 1.0
 */
public interface UserRepository {

    /**
     * Persiste un usuario (crear o actualizar).
     * 
     * @param user El usuario a persistir
     * @return El usuario persistido con ID asignado
     */
    User save(User user);

    /**
     * Busca un usuario por su ID.
     * 
     * @param id ID del usuario
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<User> findById(UserId id);

    /**
     * Busca un usuario por su email.
     * 
     * <p>
     * Usado durante la autenticación (login) y para validar unicidad de email.
     * 
     * @param email Email del usuario
     * @return Optional con el usuario si existe, vacío si no
     * @see com.casrusil.siierpai.modules.sso.application.service.AuthService
     */
    Optional<User> findByEmail(String email);

    java.util.List<User> findAllByCompanyId(com.casrusil.siierpai.shared.domain.valueobject.CompanyId companyId);
}
