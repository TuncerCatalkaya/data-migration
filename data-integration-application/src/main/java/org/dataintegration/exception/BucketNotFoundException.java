package org.dataintegration.exception;

import lombok.experimental.StandardException;

@StandardException
public class BucketNotFoundException extends DataIntegrationException {
    public BucketNotFoundException(String bucket) {
        super("Bucket " + bucket + " could not be found.");
    }
}
