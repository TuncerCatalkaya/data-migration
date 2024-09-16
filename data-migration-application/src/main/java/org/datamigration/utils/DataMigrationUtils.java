package org.datamigration.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataMigrationUtils {

    private static final DateTimeFormatter DATE_FORMATTER_TIMESTAMP = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");

    public static String getJwtUserId(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaims().get("userId"))
                .map(String::valueOf)
                .or(() -> Optional.ofNullable(jwt.getSubject()))
                .orElse(null);
    }

    public static UUID getProjectIdFromS3Key(String key) {
        return UUID.fromString(key.split("/")[0]);
    }

    public static String getFileNameFromS3Key(String key) {
        return key.split("/")[1];
    }

    public static String getTimeStamp() {
        return LocalDateTime.now().format(DATE_FORMATTER_TIMESTAMP);
    }

}
