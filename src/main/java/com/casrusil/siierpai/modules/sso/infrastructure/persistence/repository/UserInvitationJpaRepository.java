package com.casrusil.siierpai.modules.sso.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.sso.infrastructure.persistence.entity.UserInvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserInvitationJpaRepository extends JpaRepository<UserInvitationEntity, UUID> {
    Optional<UserInvitationEntity> findByToken(String token);
}
