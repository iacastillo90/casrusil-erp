package com.casrusil.siierpai.modules.sso.domain.port.out;

import com.casrusil.siierpai.modules.sso.domain.model.UserInvitation;

import java.util.Optional;

public interface UserInvitationRepository {
    UserInvitation save(UserInvitation invitation);

    Optional<UserInvitation> findByToken(String token);
}
