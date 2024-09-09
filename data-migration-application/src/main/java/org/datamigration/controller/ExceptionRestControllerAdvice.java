package org.datamigration.controller;

import org.datamigration.domain.exception.ProjectForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionRestControllerAdvice {

    @ExceptionHandler(ProjectForbiddenException.class)
    ResponseEntity<Void> handleProjectForbiddenException() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

}
