package com.casrusil.siierpai.modules.sso.infrastructure.web;

import com.casrusil.siierpai.modules.sso.application.service.UserManagementService;
import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.modules.sso.domain.model.UserRole;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para gestión de usuarios.
 * 
 * <p>
 * Permite crear nuevos usuarios y gestionar roles dentro de la empresa actual.
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 * <li>{@code POST /api/v1/users}: Crear nuevo usuario.</li>
 * <li>{@code PUT /api/v1/users/{id}/role}: Actualizar rol de usuario.</li>
 * </ul>
 * 
 * @see UserManagementService
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserManagementService userService;
    private final com.casrusil.siierpai.modules.sso.domain.port.in.InviteUserUseCase inviteUserUseCase;

    public UserController(UserManagementService userService,
            com.casrusil.siierpai.modules.sso.domain.port.in.InviteUserUseCase inviteUserUseCase) {
        this.userService = userService;
        this.inviteUserUseCase = inviteUserUseCase;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        CompanyId companyId = CompanyContext.getCompanyId();
        if (companyId == null) {
            return ResponseEntity.status(403).build();
        }
        User user = userService.createUser(request.email(), request.fullName(), request.password(), request.role(),
                companyId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<User> updateRole(@PathVariable UUID id, @RequestBody UpdateRoleRequest request) {
        User user = userService.updateUser(new UserId(id), null, request.role());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/invite")
    public ResponseEntity<Void> inviteUser(@RequestBody InviteUserRequest request) {
        CompanyId companyId = CompanyContext.getCompanyId();
        if (companyId == null) {
            return ResponseEntity.status(403).build();
        }
        inviteUserUseCase.inviteUser(request.email(), companyId, request.role().name());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/invite/validate/{token}")
    public ResponseEntity<com.casrusil.siierpai.modules.sso.domain.model.UserInvitation> validateInvitation(
            @PathVariable String token) {
        return inviteUserUseCase.validateInvitation(token)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register-invite")
    public ResponseEntity<User> registerWithInvite(@RequestBody RegisterInviteRequest request) {
        var invitationOpt = inviteUserUseCase.validateInvitation(request.token());
        if (invitationOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var invitation = invitationOpt.get();

        // Create User
        User user = userService.createUser(
                invitation.getEmail(),
                request.fullName(),
                request.password(),
                UserRole.valueOf(invitation.getRole()),
                invitation.getTargetCompanyId());

        // Mark Accepted
        inviteUserUseCase.acceptInvitation(request.token());

        return ResponseEntity.ok(user);
    }

    public record CreateUserRequest(String email, String fullName, String password, UserRole role) {
    }

    public record UpdateRoleRequest(UserRole role) {
    }

    public record InviteUserRequest(String email, UserRole role) {
    }

    public record RegisterInviteRequest(String token, String fullName, String password) {
    }
}
