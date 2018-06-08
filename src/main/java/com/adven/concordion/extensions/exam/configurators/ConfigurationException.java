package com.adven.concordion.extensions.exam.configurators;

public final class ConfigurationException extends RuntimeException {

    public ConfigurationException(final String message) {
        super(message);
    }

    public ConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
