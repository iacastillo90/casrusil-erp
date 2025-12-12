package com.casrusil.siierpai.shared.domain.valueobject;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Identificador único de compañía (Tenant).
 * 
 * <p>
 * ADR: Shared Kernel
 * </p>
 * <p>
 * Esta clase implementa el patrón Value Object de DDD. Es inmutable y encapsula
 * la identidad de una compañía, garantizando integridad referencial en todos
 * los módulos.
 * Forma parte del Núcleo Compartido según la arquitectura hexagonal definida.
 * </p>
 */
public record CompanyId(UUID value) implements Serializable {

    public CompanyId {
        if (value == null) {
            throw new IllegalArgumentException("CompanyId value cannot be null");
        }
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

    // Getter for backward compatibility with existing code that might call
    // .getValue()
    public UUID getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
