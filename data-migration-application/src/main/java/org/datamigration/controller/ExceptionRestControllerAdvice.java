package org.datamigration.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamigration.exception.BucketNotFoundException;
import org.datamigration.exception.CheckpointNotFoundException;
import org.datamigration.exception.DataMigrationException;
import org.datamigration.exception.DuplicateHostException;
import org.datamigration.exception.FileTypeNotSupportedException;
import org.datamigration.exception.HostNotFoundException;
import org.datamigration.exception.InvalidDelimiterException;
import org.datamigration.exception.InvalidUUIDException;
import org.datamigration.exception.ItemNotFoundException;
import org.datamigration.exception.KeyNotFoundException;
import org.datamigration.exception.MappingValidationException;
import org.datamigration.exception.ProjectForbiddenException;
import org.datamigration.exception.ProjectNotFoundException;
import org.datamigration.exception.ScopeNotFinishedException;
import org.datamigration.exception.ScopeNotFoundException;
import org.datamigration.exception.TagNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ExceptionRestControllerAdvice {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<List<Class<? extends DataMigrationException>>, HttpStatus> EXCEPTION_STATUS_MAPPING = Map.of(
            List.of(
                    ProjectNotFoundException.class,
                    ScopeNotFoundException.class,
                    ItemNotFoundException.class,
                    CheckpointNotFoundException.class,
                    TagNotFoundException.class,
                    BucketNotFoundException.class,
                    KeyNotFoundException.class,
                    HostNotFoundException.class
            ), HttpStatus.NOT_FOUND,
            List.of(ProjectForbiddenException.class), HttpStatus.FORBIDDEN,
            List.of(FileTypeNotSupportedException.class), HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            List.of(InvalidUUIDException.class, InvalidDelimiterException.class), HttpStatus.BAD_REQUEST,
            List.of(ScopeNotFinishedException.class), HttpStatus.TOO_EARLY,
            List.of(DuplicateHostException.class, MappingValidationException.class), HttpStatus.CONFLICT
    );

    @ExceptionHandler(DataMigrationException.class)
    ResponseEntity<String> handleUserServiceException(DataMigrationException ex) throws JsonProcessingException {
        final HttpStatus status = EXCEPTION_STATUS_MAPPING.entrySet().stream()
                .filter(entry -> entry.getKey().contains(ex.getClass()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(objectMapper.writeValueAsString(ex.getMessage()), status);
    }

}
