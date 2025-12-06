package com.casrusil.SII_ERP_AI.modules.sso.infrastructure.web;

import com.casrusil.SII_ERP_AI.modules.sso.application.service.UserManagementService;
import com.casrusil.SII_ERP_AI.modules.sso.domain.model.User;
import com.casrusil.SII_ERP_AI.modules.sso.domain.model.UserRole;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;
import com.casrusil.SII_ERP_AI.shared.domain.valueobject.UserId;
import com.casrusil.SII_ERP_AI.shared.infrastructure.context.CompanyContext;
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

    public UserController(UserManagementService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        CompanyId companyId = CompanyContext.getCompanyId();
        if (companyId == null) {
            return ResponseEntity.status(403).build();
        }
        User user = userService.createUser(request.email(), request.password(), request.role(), companyId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<User> updateRole(@PathVariable UUID id, @RequestBody UpdateRoleRequest request) {
        User user = userService.updateUser(new UserId(id), null, request.role());
        return ResponseEntity.ok(user);
    }

    public record CreateUserRequest(String email, String password, UserRole role) {
    }

    public record UpdateRoleRequest(UserRole role) {
    }
}
