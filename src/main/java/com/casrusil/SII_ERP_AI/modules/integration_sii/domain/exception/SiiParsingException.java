package com.casrusil.SII_ERP_AI.modules.integration_sii.domain.exception;

public class SiiParsingException extends RuntimeException {

    public SiiParsingException(String message) {
        super(message);
    }

    public SiiParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
