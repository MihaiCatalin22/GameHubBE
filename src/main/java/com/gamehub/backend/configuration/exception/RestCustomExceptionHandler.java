package com.gamehub.backend.configuration.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.naming.AuthenticationException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ControllerAdvice
@Slf4j
public class RestCustomExceptionHandler extends ResponseEntityExceptionHandler {

    private static final URI VALIDATION_ERROR_TYPE = URI.create("/validation-error");

    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(final AccessDeniedException error) {
        log.error("Access Denied with status {} occurred.", HttpStatus.FORBIDDEN, error);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
    }

    @ExceptionHandler(value = {AuthenticationException.class})
    public ResponseEntity<Object> handleAuthenticationException(final AuthenticationException error) {
        log.error("Authentication failed with status {} occurred.", HttpStatus.UNAUTHORIZED, error);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication Failed");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException error, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error("MethodArgumentNotValidException with status {} occurred.", HttpStatus.BAD_REQUEST, error);
        final List<ValidationErrorDTO> errors = convertToErrorsList(error);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(convertToProblemDetail(errors));
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ProblemDetail handleConstraintViolationException(final ConstraintViolationException error) {
        log.error("ConstraintViolationException with status {} occurred.", HttpStatus.BAD_REQUEST, error);
        final List<ValidationErrorDTO> errors = convertToErrorsList(error);
        return convertToProblemDetail(errors);
    }

    @ExceptionHandler(value = {ResponseStatusException.class})
    public ProblemDetail handleResponseStatusException(final ResponseStatusException error) {
        log.error("ResponseStatusException with status {} occurred.", error.getStatusCode(), error);
        final List<ValidationErrorDTO> errors = error.getReason() != null ?
                List.of(new ValidationErrorDTO(null, error.getReason()))
                : Collections.emptyList();
        return convertToProblemDetail(error.getStatusCode(), errors);
    }

    @ExceptionHandler(value = {RuntimeException.class})
    public ResponseEntity<Object> handleUnknownRuntimeError(final RuntimeException error) {
        if (error.getMessage().contains("Failed to send email")) {
            log.error("Email sending failed with status {} occurred.", HttpStatus.INTERNAL_SERVER_ERROR, error);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email. Please try again later.");
        }
        log.error("Internal server error occurred.", error);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred. Please try again later.");
    }
    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<Object> handleIllegalArgumentException(final IllegalArgumentException error) {
        log.error("IllegalArgumentException with status {} occurred.", HttpStatus.BAD_REQUEST, error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.getMessage());
    }
    @ExceptionHandler(value = {EntityNotFoundException.class})
    public ResponseEntity<Object> handleEntityNotFoundException(final EntityNotFoundException error) {
        log.error("EntityNotFoundException with status {} occurred.", HttpStatus.NOT_FOUND, error);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Friend relationship not found");
    }
    @ExceptionHandler(value = {UserNotFoundException.class})
    public ResponseEntity<Object> handleUserNotFoundException(final UserNotFoundException error) {
        log.error("UserNotFoundException with status {} occurred.", HttpStatus.NOT_FOUND, error);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
    }
    private ProblemDetail convertToProblemDetail(final List<ValidationErrorDTO> errors) {
        return convertToProblemDetail(HttpStatus.BAD_REQUEST, errors);
    }

    private ProblemDetail convertToProblemDetail(HttpStatusCode statusCode, List<ValidationErrorDTO> errors) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(statusCode);
        problemDetail.setDetail("Invalid request");
        problemDetail.setProperty("errors", errors);
        problemDetail.setType(VALIDATION_ERROR_TYPE);
        return problemDetail;
    }

    private List<ValidationErrorDTO> convertToErrorsList(final MethodArgumentNotValidException error) {
        final BindingResult bindingResult = error.getBindingResult();
        final List<ValidationErrorDTO> result = new ArrayList<>();
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors()
                    .forEach(validationError -> {
                        if (validationError instanceof final FieldError fieldError) {
                            result.add(new ValidationErrorDTO(fieldError.getField(), fieldError.getDefaultMessage()));
                        } else {
                            result.add(new ValidationErrorDTO(validationError.getObjectName(), validationError.getDefaultMessage()));
                        }
                    });
        } else {
            log.warn("MethodArgumentNotValidException without binding result errors", error);
        }
        return result;
    }

    private List<ValidationErrorDTO> convertToErrorsList(final ConstraintViolationException error) {
        if (CollectionUtils.isEmpty(error.getConstraintViolations())) {
            log.warn("Empty constraints violation for error: {}", error.getMessage());
            return Collections.emptyList();
        }

        final List<ValidationErrorDTO> result = new ArrayList<>();
        error.getConstraintViolations().forEach(constraintViolation -> {
                    final String field = constraintViolation.getPropertyPath() != null ? constraintViolation.getPropertyPath().toString() : "unknown field";
                    result.add(new ValidationErrorDTO(field, constraintViolation.getMessage()));
                }
        );
        return result;
    }

    private record ValidationErrorDTO(String field, String error) {
    }
}
