package org.datamigration.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.datamigration.exception.InvalidDelimiterException;
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
            return UUID.fromString(fastSplit(key, '/', 2)[0]);
        } catch (IllegalArgumentException ex) {
            throw new InvalidUUIDException("Provided key " + key + " does not have a valid UUID as base.");
        }
    }

    public static String getScopeKeyFromS3Key(String key) {
        return fastSplit(key, '/', 2)[1];
    }

    public static String[] fastSplit(String line, char delimiter, int arraySize) {
        final String[] result = new String[arraySize];
        int start = 0;
        int index = 0;
        int end;

        while ((end = line.indexOf(delimiter, start)) != -1) {
            result[index++] = line.substring(start, end);
            start = end + 1;
        }
        result[index] = line.substring(start);

        return result;
    }

    public static char delimiterStringToCharMapper(String delimiter) {
        return switch (delimiter) {
            case "," -> ',';
            case ";" -> ';';
            case "\\t" -> '\t';
            case "|" -> '|';
            case " " -> ' ';
            default -> throw new InvalidDelimiterException("Delimiter " + delimiter + " is invalid or not supported.");
        };

    }
}
