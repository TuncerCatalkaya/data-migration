package org.datamigration.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.datamigration.exception.InvalidUUIDException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataMigrationUtils {

    public static String getJwtUserId(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaims().get("userId"))
                .map(String::valueOf)
                .or(() -> Optional.ofNullable(jwt.getSubject()))
                .orElse(null);
    }

    public static UUID getProjectIdFromS3Key(String key) {
        try {
            return UUID.fromString(key.split("/")[0]);
        } catch (IllegalArgumentException ex) {
            throw new InvalidUUIDException("Provided key " + key + " does not have a valid UUID as base.");
        }
    }

}
