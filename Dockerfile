FROM maven:3.8.5-openjdk-17 AS build

ARG MVN_LIFECYCLE=install
ENV ENV_MVN_LIFECYCLE $MVN_LIFECYCLE

# Copy poms
COPY pom.xml .
COPY data-migration-service/pom.xml ./data-migration-service/
COPY data-migration-application/pom.xml ./data-migration-application/

# Copy src code
COPY data-migration-service/src ./data-migration-service/src
COPY data-migration-application/src ./data-migration-application/src

# Copy module
COPY data-migration-ui/ ./data-migration-ui/

# Copy extra files
COPY checkstyle.xml .
COPY lombok.config .

# Build
RUN mvn clean "$ENV_MVN_LIFECYCLE" -DskipTests

FROM maven:3.8.5-openjdk-17 AS data-migration-app
COPY --from=build data-migration-application/target/data-migration-application.jar .
EXPOSE 8001
ENTRYPOINT ["java", "-jar","/data-migration-application.jar"]
