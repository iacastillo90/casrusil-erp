package com.casrusil.SII_ERP_AI.modules.sso.domain.model;

import com.casrusil.SII_ERP_AI.shared.domain.valueobject.CompanyId;

import java.time.Instant;

/**
 * Entidad raíz del agregado Company en el contexto de SSO/Multi-tenancy.
 * 
 * <p>
 * Representa una empresa registrada en el sistema ERP. Cada empresa es un
 * tenant
 * independiente con sus propios datos aislados (facturas, contabilidad,
 * usuarios).
 * 
 * <h2>Invariantes:</h2>
 * <ul>
 * <li>El RUT debe ser único en el sistema</li>
 * <li>El RUT debe tener formato válido chileno (ej: "76.123.456-7")</li>
 * <li>Una empresa puede estar activa o inactiva (soft delete)</li>
 * <li>El ID es inmutable una vez creado</li>
 * </ul>
 * 
 * <h2>Ciclo de vida:</h2>
 * <ol>
 * <li>Creación: {@link #create(String, String, String)}</li>
 * <li>Activación/Desactivación: {@link #activate()}, {@link #deactivate()}</li>
 * <li>Actualización: {@link #updateProfile(String, String)}</li>
 * </ol>
 * 
 * <h2>Eventos del dominio:</h2>
 * <ul>
 * <li>{@link com.casrusil.SII_ERP_AI.modules.sso.domain.event.CompanyCreatedEvent}
 * - Al crear empresa</li>
 * </ul>
 * 
 * @see CompanyId
 * @see com.casrusil.SII_ERP_AI.modules.sso.domain.event.CompanyCreatedEvent
 * @since 1.0
 */
public class Company {
    private final CompanyId id;
    private String rut;
    private String razonSocial;
    private String email;
    private boolean isActive;
    private final Instant createdAt;

    public Company(CompanyId id, String rut, String razonSocial, String email, boolean isActive, Instant createdAt) {
        this.id = id;
        this.rut = rut;
        this.razonSocial = razonSocial;
        this.email = email;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    /**
     * Crea una nueva empresa con los datos básicos.
     * 
     * @param rut         RUT de la empresa
     * @param razonSocial Razón social o nombre legal
     * @param email       Correo electrónico de contacto principal
     * @return Nueva instancia de Company
     */
    public static Company create(String rut, String razonSocial, String email) {
        return new Company(CompanyId.generate(), rut, razonSocial, email, true, Instant.now());
    }

    /**
     * Activa la empresa.
     * 
     * <p>
     * Una empresa activa puede acceder al sistema y realizar operaciones.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Desactiva la empresa (soft delete).
     * 
     * <p>
     * Una empresa desactivada no puede acceder al sistema pero sus datos
     * se mantienen para auditoría e histórico.
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Actualiza el perfil de la empresa.
     * 
     * @param razonSocial Nueva razón social
     * @param email       Nuevo correo electrónico
     */
    public void updateProfile(String razonSocial, String email) {
        this.razonSocial = razonSocial;
        this.email = email;
    }

    public CompanyId getId() {
        return id;
    }

    public String getRut() {
        return rut;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return isActive;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
