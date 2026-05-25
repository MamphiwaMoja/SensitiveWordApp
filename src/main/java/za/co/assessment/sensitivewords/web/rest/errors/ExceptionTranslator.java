package za.co.assessment.sensitivewords.web.rest.errors;

import brave.Span;
import brave.Tracer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import za.co.assessment.sensitivewords.dto.response.ErrorResponse;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionTranslator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionTranslator.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final Tracer tracer;

    public ExceptionTranslator(Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateSensitiveWordException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateSensitiveWordException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        // Keep field errors ordered for predictable responses and easier test assertions.
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        return build(HttpStatus.BAD_REQUEST, ErrorMessages.VALIDATION_FAILED, request, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, resolveNotReadableMessage(ex), request, resolveNotReadableDetails(ex));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        Map<String, String> details = Map.of(
                "parameter", ex.getName(),
                "rejectedValue", String.valueOf(ex.getValue())
        );
        return build(
                HttpStatus.BAD_REQUEST,
                "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'",
                request,
                details
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ErrorMessages.DATABASE_CONSTRAINT_VIOLATION, request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        LOGGER.error("Unhandled exception for path={}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.UNEXPECTED_SERVER_ERROR, request, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request, Object details) {
        // Keep every error payload consistent so clients can handle validation and server errors uniformly.
        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                details
        );

        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status);
        // Error responses carry the same trace id header as successful controller responses.
        String traceId = currentTraceId();
        if (traceId != null) {
            builder.header(TRACE_ID_HEADER, traceId);
        }
        return builder.body(response);
    }

    private String currentTraceId() {
        Span span = tracer.currentSpan();
        return span == null ? null : span.context().traceIdString();
    }

    private String resolveNotReadableMessage(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof InvalidFormatException invalidFormat) {
            String fieldName = resolveFieldName(invalidFormat);
            if (invalidFormat.getTargetType() != null && invalidFormat.getTargetType().isEnum()) {
                // Enum parse errors are common client mistakes, so return a direct field-level message.
                return "Invalid value '" + invalidFormat.getValue() + "' for field '" + fieldName + "'";
            }
        }

        return ErrorMessages.MALFORMED_JSON_REQUEST;
    }

    private Object resolveNotReadableDetails(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (!(cause instanceof InvalidFormatException invalidFormat)) {
            return null;
        }

        Map<String, Object> details = new LinkedHashMap<>();
        String fieldName = resolveFieldName(invalidFormat);
        details.put("field", fieldName);
        details.put("rejectedValue", invalidFormat.getValue());

        Class<?> targetType = invalidFormat.getTargetType();
        if (targetType != null && targetType.isEnum()) {
            details.put(
                    "allowedValues",
                    Arrays.stream(targetType.getEnumConstants())
                            .map(String::valueOf)
                            .collect(Collectors.toList())
            );
        }

        return details;
    }

    private String resolveFieldName(JsonMappingException exception) {
        // Jackson can report nested paths; for this API the first field name is the clearest client-facing signal.
        return exception.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(fieldName -> fieldName != null && !fieldName.isBlank())
                .findFirst()
                .orElse("request");
    }
}
