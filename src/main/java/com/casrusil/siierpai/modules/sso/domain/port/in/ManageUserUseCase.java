package com.casrusil.siierpai.modules.sso.domain.port.in;

import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.modules.sso.domain.model.UserRole;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;

/**
 * Caso de uso para gestionar usuarios del sistema.
 * 
 * <p>
 * Este contrato define las operaciones de gestión de usuarios, incluyendo
 * creación, actualización y cambio de contraseña. Cada usuario pertenece a una
 * empresa específica (multi-tenancy) y tiene un rol asignado.
 * 
 * <h2>Responsabilidades:</h2>
 * <ul>
 * <li>Crear nuevos usuarios con credenciales seguras (BCrypt)</li>
 * <li>Actualizar información de usuarios existentes</li>
 * <li>Gestionar cambios de contraseña con validación</li>
 * <li>Asignar roles (ADMIN, ACCOUNTANT, VIEWER)</li>
 * </ul>
 * 
 * <h2>Seguridad:</h2>
 * <ul>
 * <li>Las contraseñas se almacenan hasheadas con BCrypt</li>
 * <li>Cada usuario está aislado por empresa (CompanyContext)</li>
 * <li>Los roles determinan los permisos de acceso</li>
 * </ul>
 * 
 * <h2>Ejemplo de uso:</h2>
 * 
 * <pre>{@code
 * // Crear nuevo usuario
 * User user = manageUserUseCase.createUser(
 *         "contador@acme.cl",
 *         "SecurePassword123!",
 *         UserRole.ACCOUNTANT,
 *         companyId);
 * 
 * // Cambiar contraseña
 * manageUserUseCase.changePassword(
 *         user.getId(),
 *         "SecurePassword123!",
 *         "NewPassword456!");
 * }</pre>
 * 
 * @see User
 * @see UserRole
 * @see com.casrusil.siierpai.modules.sso.application.service.UserManagementService
 * @since 1.0
 */
public interface ManageUserUseCase {

    /**
     * Crea un nuevo usuario en el sistema.
     * 
     * <p>
     * La contraseña se hashea automáticamente usando BCrypt antes de almacenarla.
     * El usuario queda asociado a la empresa especificada.
     * 
     * @param email       Email del usuario (debe ser único en el sistema)
     * @param rawPassword Contraseña en texto plano (será hasheada)
     * @param role        Rol del usuario (ADMIN, ACCOUNTANT, VIEWER)
     * @param companyId   ID de la empresa a la que pertenece el usuario
     * @return El usuario recién creado
     * @throws IllegalArgumentException si el email ya existe o es inválido
     * @see com.casrusil.siierpai.modules.sso.infrastructure.security.PasswordEncoder
     */
    User createUser(String email, String fullName, String rawPassword, UserRole role, CompanyId companyId);

    /**
     * Actualiza la información de un usuario existente.
     * 
     * <p>
     * Permite cambiar el email y el rol. Para cambiar la contraseña,
     * usar {@link #changePassword(UserId, String, String)}.
     * 
     * @param id    ID del usuario a actualizar
     * @param email Nuevo email
     * @param role  Nuevo rol
     * @return El usuario actualizado
     * @throws IllegalArgumentException si el usuario no existe o el email está en
     *                                  uso
     */
    User updateUser(UserId id, String email, UserRole role);

    /**
     * Cambia la contraseña de un usuario.
     * 
     * <p>
     * Valida que la contraseña antigua sea correcta antes de establecer la nueva.
     * La nueva contraseña se hashea automáticamente con BCrypt.
     * 
     * @param id          ID del usuario
     * @param oldPassword Contraseña actual (para validación)
     * @param newPassword Nueva contraseña en texto plano
     * @throws IllegalArgumentException si el usuario no existe
     * @throws SecurityException        si la contraseña antigua es incorrecta
     */
    void changePassword(UserId id, String oldPassword, String newPassword);

    java.util.List<User> getUsersByCompany(com.casrusil.siierpai.shared.domain.valueobject.CompanyId companyId);
}
