package com.casrusil.siierpai.modules.sso.domain.port.in;

import com.casrusil.siierpai.modules.sso.domain.model.UserInvitation;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.Optional;

public interface InviteUserUseCase {
    UserInvitation inviteUser(String email, CompanyId companyId, String role);

    Optional<UserInvitation> validateInvitation(String token);

    void acceptInvitation(String token);
}
