package org.datamigration.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataMigrationUtils {

    public static Integer getJwtUserId(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaims().get("userId"))
                .map(String::valueOf)
                .or(() -> Optional.ofNullable(jwt.getSubject()))
                .map(Integer::valueOf)
                .orElse(null);
    }

}
