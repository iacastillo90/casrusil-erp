package com.casrusil.siierpai.modules.sso.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.sso.domain.model.UserInvitation;
import com.casrusil.siierpai.modules.sso.domain.port.out.UserInvitationRepository;
import com.casrusil.siierpai.modules.sso.infrastructure.persistence.entity.UserInvitationEntity;
import com.casrusil.siierpai.modules.sso.infrastructure.persistence.repository.UserInvitationJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserInvitationJpaAdapter implements UserInvitationRepository {

    private final UserInvitationJpaRepository jpaRepository;

    public UserInvitationJpaAdapter(UserInvitationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserInvitation save(UserInvitation invitation) {
        UserInvitationEntity entity = toEntity(invitation);
        UserInvitationEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<UserInvitation> findByToken(String token) {
        return jpaRepository.findByToken(token).map(this::toDomain);
    }

    private UserInvitationEntity toEntity(UserInvitation model) {
        return new UserInvitationEntity(
                model.getId(),
                model.getEmail(),
                model.getTargetCompanyId().getValue(),
                model.getRole(),
                model.getToken(),
                model.getExpiresAt(),
                model.getStatus());
    }

    private UserInvitation toDomain(UserInvitationEntity entity) {
        return new UserInvitation(
                entity.getId(),
                entity.getEmail(),
                new CompanyId(entity.getTargetCompanyId()),
                entity.getRole(),
                entity.getToken(),
                entity.getExpiresAt(),
                entity.getStatus());
    }
}
