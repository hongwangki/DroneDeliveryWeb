package drone.delivery.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String code;
    private final String message;
    private final String path;
    private final List<ValidationError> errors;

    public ErrorResponse(int status, String code, String message, String path, List<ValidationError> errors) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
        this.errors = (errors == null || errors.isEmpty()) ? null : errors;
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, HttpServletRequest request) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                (message != null && !message.isBlank()) ? message : errorCode.getDefaultMessage(),
                request.getRequestURI(),
                null
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, HttpServletRequest request, BindingResult binding) {
        List<ValidationError> errors = binding.getFieldErrors().stream()
                .map(ValidationError::from)
                .collect(Collectors.toList());
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                (message != null && !message.isBlank()) ? message : errorCode.getDefaultMessage(),
                request.getRequestURI(),
                errors
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, HttpServletRequest request, java.util.Set<ConstraintViolation<?>> violations) {
        List<ValidationError> errors = violations.stream()
                .map(ValidationError::from)
                .collect(Collectors.toList());
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                (message != null && !message.isBlank()) ? message : errorCode.getDefaultMessage(),
                request.getRequestURI(),
                errors
        );
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public List<ValidationError> getErrors() { return errors; }

    public static class ValidationError {
        private final String field;
        private final String rejectedValue;
        private final String reason;

        public ValidationError(String field, String rejectedValue, String reason) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.reason = reason;
        }

        public static ValidationError from(FieldError fe) {
            Object rv = fe.getRejectedValue();
            return new ValidationError(fe.getField(), rv == null ? null : String.valueOf(rv), fe.getDefaultMessage());
        }

        public static ValidationError from(ConstraintViolation<?> v) {
            String field = v.getPropertyPath() == null ? null : v.getPropertyPath().toString();
            Object rv = v.getInvalidValue();
            return new ValidationError(field, rv == null ? null : String.valueOf(rv), v.getMessage());
        }

        public String getField() { return field; }
        public String getRejectedValue() { return rejectedValue; }
        public String getReason() { return reason; }
    }
}
