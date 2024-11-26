package org.dataintegration.exception;

import lombok.experimental.StandardException;

@StandardException
public class KeyNotFoundException extends DataIntegrationException {
    public KeyNotFoundException(String key) {
        super("Key " + key + " could not be found.");
    }
}
