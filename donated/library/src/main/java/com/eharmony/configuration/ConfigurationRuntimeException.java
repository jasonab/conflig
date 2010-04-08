package com.eharmony.configuration;

/**
 * So we can thrown our own runtime exception.
 * User: fwu
 */
public class ConfigurationRuntimeException  extends RuntimeException {
    public ConfigurationRuntimeException() {
    }

    public ConfigurationRuntimeException(String message) {
        super(message);
    }

    public ConfigurationRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationRuntimeException(Throwable cause) {
        super(cause);
    }
}
