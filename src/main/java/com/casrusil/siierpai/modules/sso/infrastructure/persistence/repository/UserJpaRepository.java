package com.casrusil.siierpai.modules.sso.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.sso.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para usuarios.
 * 
 * <p>
 * Facilita la gestión de usuarios y la autenticación mediante búsqueda por
 * email.
 * 
 * @since 1.0
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
}
