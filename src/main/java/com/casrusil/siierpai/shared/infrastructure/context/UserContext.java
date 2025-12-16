package com.casrusil.siierpai.shared.infrastructure.context;

import com.casrusil.siierpai.shared.domain.valueobject.UserId;

/**
 * Contexto de usuario para almacenar la identidad del usuario actual de forma
 * thread-safe.
 * 
 * <p>
 * Utiliza ScopedValue (Java 21) para mantener el ID y el Nombre del usuario
 * durante el ciclo de vida de la request.
 */
public class UserContext {
    public static final ScopedValue<UserId> USER_ID = ScopedValue.newInstance();
    public static final ScopedValue<String> USER_NAME = ScopedValue.newInstance();

    public static UserId getUserId() {
        return USER_ID.isBound() ? USER_ID.get() : null;
    }

    public static String getUserName() {
        return USER_NAME.isBound() ? USER_NAME.get() : null;
    }

    public static void runInUserContext(UserId userId, String userName, Runnable action) {
        ScopedValue.where(USER_ID, userId)
                .where(USER_NAME, userName)
                .run(action);
    }
}
