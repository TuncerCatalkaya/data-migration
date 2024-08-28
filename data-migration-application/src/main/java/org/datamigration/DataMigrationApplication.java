package org.datamigration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Data migration application launcher.
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@EnableJpaRepositories
@SpringBootApplication
@lombok.Generated // skip coverage
public class DataMigrationApplication {

    /**
     * Traditional main method. Launches spring boot application.
     *
     * @param args start arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DataMigrationApplication.class, args);
    }
}