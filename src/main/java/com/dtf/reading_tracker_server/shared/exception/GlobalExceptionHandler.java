package com.dtf.reading_tracker_server.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Database constraint rejected request: {}", ex.getMostSpecificCause().getMessage());
        return error(HttpStatus.CONFLICT, "Request conflicts with an existing resource", request);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalService(ExternalServiceException ex, HttpServletRequest request) {
        log.warn("External service failure: {}", ex.getMessage(), ex);
        return error(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();

        List<ErrorResponse.FieldError> objectErrors = ex.getBindingResult()
                .getGlobalErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        List<ErrorResponse.FieldError> allErrors = new java.util.ArrayList<>(fieldErrors);
        allErrors.addAll(objectErrors);

        return error(HttpStatus.BAD_REQUEST, "Request validation failed", request, allErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> new ErrorResponse.FieldError(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();

        return error(HttpStatus.BAD_REQUEST, "Request validation failed", request, fieldErrors);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, readableMessage(ex), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        HttpStatus responseStatus = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ex.getReason() != null ? ex.getReason() : responseStatus.getReasonPhrase();
        return error(responseStatus, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled request failure", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ErrorResponse.FieldError toFieldError(ObjectError error) {
        return new ErrorResponse.FieldError(error.getObjectName(), error.getDefaultMessage());
    }

    private String readableMessage(Exception ex) {
        if (ex instanceof MissingServletRequestParameterException missingParameter) {
            return "Missing required parameter: " + missingParameter.getParameterName();
        }

        if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            return "Invalid value for parameter: " + mismatch.getName();
        }

        if (ex instanceof HttpMessageNotReadableException) {
            return "Malformed request body";
        }

        return "Invalid request";
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        return error(status, message, request, List.of());
    }

    private ResponseEntity<ErrorResponse> error(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<ErrorResponse.FieldError> fieldErrors
    ) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        request.getRequestURI(),
                        request.getHeader("X-Trace-Id"),
                        fieldErrors
                ));
    }
}
