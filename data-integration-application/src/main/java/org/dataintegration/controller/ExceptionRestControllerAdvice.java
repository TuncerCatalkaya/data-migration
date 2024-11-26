package org.dataintegration.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dataintegration.exception.BucketNotFoundException;
import org.dataintegration.exception.CheckpointNotFoundException;
import org.dataintegration.exception.DataIntegrationException;
import org.dataintegration.exception.DatabaseNotFoundException;
import org.dataintegration.exception.DuplicateHostException;
import org.dataintegration.exception.FileTypeNotSupportedException;
import org.dataintegration.exception.HostNotFoundException;
import org.dataintegration.exception.InvalidDelimiterException;
import org.dataintegration.exception.InvalidUUIDException;
import org.dataintegration.exception.ItemNotFoundException;
import org.dataintegration.exception.KeyNotFoundException;
import org.dataintegration.exception.MappedItemNotFoundException;
import org.dataintegration.exception.MappingNotFoundException;
import org.dataintegration.exception.MappingValidationException;
import org.dataintegration.exception.ProjectForbiddenException;
import org.dataintegration.exception.ProjectNotFoundException;
import org.dataintegration.exception.ScopeNotFinishedException;
import org.dataintegration.exception.ScopeNotFoundException;
import org.dataintegration.exception.ScopeValidationException;
import org.dataintegration.exception.TagNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ExceptionRestControllerAdvice {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<List<Class<? extends DataIntegrationException>>, HttpStatus> EXCEPTION_STATUS_MAPPING = Map.of(
            List.of(
                    ProjectNotFoundException.class,
                    ScopeNotFoundException.class,
                    ItemNotFoundException.class,
                    CheckpointNotFoundException.class,
                    HostNotFoundException.class,
                    DatabaseNotFoundException.class,
                    MappingNotFoundException.class,
                    MappedItemNotFoundException.class,
                    BucketNotFoundException.class,
                    KeyNotFoundException.class,
                    TagNotFoundException.class
            ), HttpStatus.NOT_FOUND,
            List.of(ProjectForbiddenException.class), HttpStatus.FORBIDDEN,
            List.of(FileTypeNotSupportedException.class), HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            List.of(InvalidUUIDException.class, InvalidDelimiterException.class), HttpStatus.BAD_REQUEST,
            List.of(ScopeNotFinishedException.class), HttpStatus.TOO_EARLY,
            List.of(
                    DuplicateHostException.class,
                    ScopeValidationException.class,
                    MappingValidationException.class
            ), HttpStatus.CONFLICT
    );

    @ExceptionHandler(DataIntegrationException.class)
    ResponseEntity<String> handleUserServiceException(DataIntegrationException ex) throws JsonProcessingException {
        final HttpStatus status = EXCEPTION_STATUS_MAPPING.entrySet().stream()
                .filter(entry -> entry.getKey().contains(ex.getClass()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(objectMapper.writeValueAsString(ex.getMessage()), status);
    }

}
