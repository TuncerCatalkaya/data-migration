package org.datamigration.exception;

import lombok.experimental.StandardException;

@StandardException
public class BucketNotFoundException extends DataMigrationException {
    public BucketNotFoundException(String bucket) {
        super("Bucket " + bucket + " could not be found.");
    }
}
