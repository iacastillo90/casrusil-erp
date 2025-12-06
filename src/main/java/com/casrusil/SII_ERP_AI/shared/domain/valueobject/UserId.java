package com.casrusil.SII_ERP_AI.shared.domain.valueobject;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de un usuario.
 * 
 * <p>
 * Implementa el patrón Value Object de DDD para evitar la obsesión por
 * primitivos.
 * Similar a {@link CompanyId}, proporciona tipado fuerte para IDs de usuario.
 * 
 * <h2>Características:</h2>
 * <ul>
 * <li>Inmutable - No puede cambiar después de creado</li>
 * <li>Tipado fuerte - Evita confundir con otros IDs</li>
 * <li>Serializable - Para uso en sesiones HTTP</li>
 * <li>Equality por valor - Dos UserId son iguales si sus UUIDs son iguales</li>
 * </ul>
 * 
 * @see com.casrusil.SII_ERP_AI.modules.sso.domain.model.User
 * @see CompanyId
 * @since 1.0
 */
public class UserId implements Serializable {
    private final UUID value;

    public UserId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("UserId value cannot be null");
        }
        this.value = value;
    }

    /**
     * Genera un nuevo ID de usuario aleatorio.
     * 
     * @return Nueva instancia de UserId
     */
    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId random() {
        return generate();
    }

    public static UserId fromString(String uuid) {
        return new UserId(UUID.fromString(uuid));
    }

    public static UserId of(UUID uuid) {
        return new UserId(uuid);
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
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
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
