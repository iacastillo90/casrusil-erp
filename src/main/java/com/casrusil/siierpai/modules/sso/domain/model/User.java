package com.casrusil.siierpai.modules.sso.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Entidad User del agregado Company en el contexto de SSO.
 * 
 * <p>
 * Representa un usuario del sistema vinculado a una empresa específica.
 * Cada usuario tiene credenciales únicas y un rol que determina sus permisos.
 * 
 * <h2>Invariantes:</h2>
 * <ul>
 * <li>El email debe ser único a nivel global</li>
 * <li>La contraseña se almacena hasheada con BCrypt (nunca en texto plano)</li>
 * <li>Cada usuario pertenece a exactamente una empresa</li>
 * <li>El rol determina los permisos de acceso</li>
 * </ul>
 * 
 * <h2>Seguridad:</h2>
 * <ul>
 * <li>Contraseñas hasheadas con BCrypt (costo 10)</li>
 * <li>Aislamiento por empresa (CompanyContext)</li>
 * <li>Roles: ADMIN, ACCOUNTANT, VIEWER</li>
 * </ul>
 * 
 * <h2>Ciclo de vida:</h2>
 * <ol>
 * <li>Creación: {@link #create(String, String, UserRole, CompanyId)}</li>
 * <li>Cambio de contraseña: {@link #changePassword(String)}</li>
 * <li>Cambio de rol: {@link #updateRole(UserRole)}</li>
 * <li>Activación/Desactivación: {@link #activate()}, {@link #deactivate()}</li>
 * </ol>
 * 
 * @see UserId
 * @see UserRole
 * @see CompanyId
 * @since 1.0
 */
public class User {
    private final UserId id;
    private String email;
    private String fullName;
    private String passwordHash;
    private UserRole role;
    private final CompanyId companyId;
    private boolean isActive;
    private final Instant createdAt;
    private Map<String, String> preferences;

    public User(UserId id, String email, String fullName, String passwordHash, UserRole role, CompanyId companyId,
            boolean isActive,
            Instant createdAt, Map<String, String> preferences) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.role = role;
        this.companyId = companyId;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.preferences = preferences != null ? preferences : new HashMap<>();
    }

    /**
     * Crea un nuevo usuario.
     * 
     * @param email        Correo electrónico (nombre de usuario)
     * @param passwordHash Hash de la contraseña (BCrypt)
     * @param role         Rol del usuario (ADMIN, USER, ACCOUNTANT)
     * @param companyId    ID de la empresa a la que pertenece
     * @return Nueva instancia de User
     */
    public static User create(String email, String fullName, String passwordHash, UserRole role, CompanyId companyId) {
        return new User(UserId.generate(), email, fullName, passwordHash, role, companyId, true, Instant.now(),
                new HashMap<>());
    }

    // ... existing methods ...

    public Map<String, String> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, String> preferences) {
        this.preferences = preferences;
    }

    public void setPreference(String key, String value) {
        this.preferences.put(key, value);
    }

    public String getPreference(String key) {
        return this.preferences.get(key);
    }

    /**
     * Cambia la contraseña del usuario.
     * 
     * @param newPasswordHash Nuevo hash de contraseña
     */
    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    /**
     * Actualiza el rol del usuario.
     * 
     * @param newRole Nuevo rol (ADMIN, ACCOUNTANT, VIEWER)
     */
    public void updateRole(UserRole newRole) {
        this.role = newRole;
    }

    /**
     * Activa el usuario.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Desactiva el usuario (soft delete).
     */
    public void deactivate() {
        this.isActive = false;
    }

    public UserId getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public boolean isActive() {
        return isActive;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
