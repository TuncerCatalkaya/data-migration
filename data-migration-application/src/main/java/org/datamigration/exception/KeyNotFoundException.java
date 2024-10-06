package org.datamigration.exception;

import lombok.experimental.StandardException;

@StandardException
public class KeyNotFoundException extends DataMigrationException {
    public KeyNotFoundException(String key) {
        super("Key " + key + " could not be found.");
    }
}
