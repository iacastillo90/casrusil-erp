package com.casrusil.SII_ERP_AI.shared.domain.valueobject;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de una empresa.
 * 
 * <p>
 * Implementa el patrón Value Object de DDD para evitar la obsesión por
 * primitivos
 * y proporcionar tipado fuerte. Dos CompanyId son iguales si sus UUIDs son
 * iguales.
 * 
 * <h2>Características:</h2>
 * <ul>
 * <li>Inmutable - No puede cambiar después de creado</li>
 * <li>Tipado fuerte - Evita confundir con otros IDs</li>
 * <li>Serializable - Para uso en sesiones y caché</li>
 * <li>Equality por valor - No por referencia</li>
 * </ul>
 * 
 * <h2>Uso en Multi-tenancy:</h2>
 * <p>
 * Este ID es fundamental para el aislamiento de datos entre empresas.
 * Se usa en
 * {@link com.casrusil.SII_ERP_AI.shared.infrastructure.context.CompanyContext}
 * para filtrar automáticamente todas las consultas.
 * 
 * @see com.casrusil.SII_ERP_AI.modules.sso.domain.model.Company
 * @see com.casrusil.SII_ERP_AI.shared.infrastructure.context.CompanyContext
 * @since 1.0
 */
public class CompanyId implements Serializable {
    private final UUID value;

    public CompanyId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("CompanyId value cannot be null");
        }
        this.value = value;
    }

    /**
     * Genera un nuevo ID de empresa aleatorio.
     * 
     * @return Nueva instancia de CompanyId
     */
    public static CompanyId generate() {
        return new CompanyId(UUID.randomUUID());
    }

    public static CompanyId random() {
        return generate();
    }

    public static CompanyId fromString(String uuid) {
        return new CompanyId(UUID.fromString(uuid));
    }

    public static CompanyId of(UUID uuid) {
        return new CompanyId(uuid);
    }

    public UUID value() {
        return value;
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CompanyId companyId = (CompanyId) o;
        return Objects.equals(value, companyId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
