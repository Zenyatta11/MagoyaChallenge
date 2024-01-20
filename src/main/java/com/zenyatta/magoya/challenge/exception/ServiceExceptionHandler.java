package com.zenyatta.magoya.challenge.exception;

import com.eventstore.dbclient.StreamNotFoundException;
import com.eventstore.dbclient.WrongExpectedVersionException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;

@ControllerAdvice
@Slf4j
public class ServiceExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> illegalArgumentException(final IllegalArgumentException exception) {
        log.error("Illegal argument", exception);
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> entityNotFoundException(final EntityNotFoundException exception) {
        log.error("Entity not found", exception);
        return ResponseEntity.notFound().build();
    }

    // @ExceptionHandler(StreamNotFoundException.class)
    // public ResponseEntity<Void> streamNotFoundException(final StreamNotFoundException exception) {
    //     log.error("Stream not found", exception);
    //     return ResponseEntity.notFound().build();
    // }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> illegalStateException(final IllegalStateException exception) {
        log.error("Illegal state", exception);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(WrongExpectedVersionException.class)
    public ResponseEntity<String> wrongExpectedVersionException(final WrongExpectedVersionException exception) {
        log.error("Wrong version", exception.getActualVersion());
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
            .body("Wrong version provided.");
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Void> wrongRestClientException(final RestClientException exception) {
        log.error("Wrong REST client", exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
