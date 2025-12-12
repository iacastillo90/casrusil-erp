package com.casrusil.siierpai.modules.sso.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.modules.sso.domain.port.out.UserRepository;
import com.casrusil.siierpai.modules.sso.infrastructure.persistence.entity.UserEntity;
import com.casrusil.siierpai.modules.sso.infrastructure.persistence.repository.UserJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de persistencia para usuarios.
 * 
 * <p>
 * Implementa {@link UserRepository} para el acceso a datos de usuarios y roles.
 * 
 * @since 1.0
 */
@Component
public class UserJpaAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserJpaAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    private UserEntity toEntity(User user) {
        return new UserEntity(
                user.getId().getValue(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                user.getCompanyId().getValue(),
                user.isActive(),
                user.getCreatedAt());
    }

    private User toDomain(UserEntity entity) {
        return new User(
                new UserId(entity.getId()),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole(),
                new CompanyId(entity.getCompanyId()),
                entity.isActive(),
                entity.getCreatedAt());
    }
}
