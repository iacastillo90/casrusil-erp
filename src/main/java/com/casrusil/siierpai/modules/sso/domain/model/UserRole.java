package com.casrusil.siierpai.modules.sso.domain.model;

/**
 * Roles de usuario en el sistema ERP.
 * 
 * <p>
 * Define los niveles de acceso y permisos de los usuarios.
 * Cada rol tiene diferentes capacidades en el sistema.
 * 
 * <h2>Roles disponibles:</h2>
 * <ul>
 * <li><b>ADMIN</b> - Acceso completo al sistema, puede gestionar usuarios y
 * configuración</li>
 * <li><b>ACCOUNTANT</b> - Acceso a módulos contables, puede crear/corregir
 * asientos</li>
 * <li><b>USER</b> - Acceso de solo lectura, puede ver reportes</li>
 * </ul>
 * 
 * @see com.casrusil.siierpai.modules.sso.domain.model.User
 * @since 1.0
 */
public enum UserRole {
    ADMIN,
    USER,
    ACCOUNTANT
}
