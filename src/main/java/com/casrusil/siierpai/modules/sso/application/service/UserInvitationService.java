package com.casrusil.siierpai.modules.sso.application.service;

import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.model.UserInvitation;
import com.casrusil.siierpai.modules.sso.domain.port.in.InviteUserUseCase;
import com.casrusil.siierpai.modules.sso.domain.port.in.ManageCompanyUseCase;
import com.casrusil.siierpai.modules.sso.domain.port.out.UserInvitationRepository;
import com.casrusil.siierpai.modules.sso.domain.port.out.UserRepository;
import com.casrusil.siierpai.shared.infrastructure.mail.EmailService;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserInvitationService implements InviteUserUseCase {

    private final UserInvitationRepository invitationRepository;
    private final EmailService emailService;
    private final ManageCompanyUseCase companyService;
    private final UserRepository userRepository;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public UserInvitationService(UserInvitationRepository invitationRepository, EmailService emailService,
            ManageCompanyUseCase companyService, UserRepository userRepository) {
        this.invitationRepository = invitationRepository;
        this.emailService = emailService;
        this.companyService = companyService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserInvitation inviteUser(String email, CompanyId companyId, String role) {
        // TODO: Check if user already exists or already invited? For MVP we just create
        // new invite.

        UserInvitation invitation = UserInvitation.create(email, companyId, role);
        UserInvitation savedInvitation = invitationRepository.save(invitation);

        Company company = companyService.getCompany(companyId);

        // Fetch Inviter logic
        // We assume the current user is the inviter.
        // Since we are in a Service, we can check SecurityContext or UserContext.
        // Ideally UserContext should populate this, but for now we look up by ID from
        // Security (which SecurityFilter sets).
        // If system call (no user), fallback to "System" or Company Name.
        String inviterName = "Un administrador";

        // Try to get from SecurityContext
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserId userId) {
            userRepository.findById(userId).ifPresent(u -> {
                // We can't access non-final local variable inviterName safely in lambda if we
                // assign.
                // So we need another way or just use Optional mapping.
            });
            // Better:
            inviterName = userRepository.findById(userId)
                    .map(u -> u.getFullName() != null ? u.getFullName() : u.getEmail())
                    .orElse("Un administrador");
        }

        String inviteLink = frontendUrl + "/register?token=" + savedInvitation.getToken();

        emailService.sendInvitationEmail(email, inviteLink, company, inviterName);

        return savedInvitation;
    }

    @Override
    public Optional<UserInvitation> validateInvitation(String token) {
        return invitationRepository.findByToken(token)
                .filter(UserInvitation::isValid);
    }

    @Override
    @Transactional
    public void acceptInvitation(String token) {
        invitationRepository.findByToken(token).ifPresent(invitation -> {
            invitation.accept();
            invitationRepository.save(invitation);
        });
    }
}
