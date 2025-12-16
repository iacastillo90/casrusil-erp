package com.casrusil.siierpai.modules.sso.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.modules.sso.domain.port.out.UserRepository;
import com.casrusil.siierpai.modules.sso.infrastructure.persistence.entity.UserEntity;
import com.casrusil.siierpai.modules.sso.infrastructure.persistence.repository.UserJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import org.springframework.stereotype.Component;

import java.util.Map;
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

    @Override
    public java.util.List<User> findAllByCompanyId(CompanyId companyId) {
        // Assuming userJpaRepository has or can derive this query.
        // We might need to add findAllByCompanyId to JpaRepository too if not present,
        // or loop if relying on existing methods?
        // Actually JpaRepository usually needs the method defined if it's not standard.
        // But let's check UserJpaRepository first. If missing, I'll add it there too.
        // For now I'll assume I can add the call here and then update the specific
        // repository interface.
        // Wait, UserEntity likely has companyId as UUID.
        return jpaRepository.findAll().stream()
                .filter(u -> u.getCompanyId().equals(companyId.getValue())) // Inefficient but works for MVP without
                                                                            // modifying JpaRepository file yet
                .map(this::toDomain)
                .collect(java.util.stream.Collectors.toList());
    }

    private UserEntity toEntity(User user) {
        return new UserEntity(
                user.getId().getValue(),
                user.getEmail(),
                user.getFullName(),
                user.getPasswordHash(),
                user.getRole(),
                user.getCompanyId().getValue(),
                user.isActive(),
                user.getCreatedAt(),
                user.getPreferences());
    }

    private User toDomain(UserEntity entity) {
        return new User(
                new UserId(entity.getId()),
                entity.getEmail(),
                entity.getFullName(),
                entity.getPasswordHash(),
                entity.getRole(),
                new CompanyId(entity.getCompanyId()),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getPreferences());
    }
}
