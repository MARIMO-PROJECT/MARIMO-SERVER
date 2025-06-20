package com.marimo.server.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;
import org.springframework.validation.BindingResult;

@JsonInclude(Include.NON_NULL)
public record ErrorResponse(
        int code,
        String message,
        Object errors
) {

    public static ErrorResponse fail(final ErrorType errorType) {
        return new ErrorResponse(errorType.getCode(), errorType.getMessage(), null);
    }

    public static ErrorResponse fail(final ErrorType errorType, final String errorDetail) {
        return new ErrorResponse(errorType.getCode(), errorType.getMessage() + "(" + errorDetail + ")", null);
    }

    public static ErrorResponse fail(final ErrorType errorType, final Set<ConstraintViolation<?>> violations) {
        return new ErrorResponse(errorType.getCode(), errorType.getMessage(), ValidationError.of(violations));
    }

    public static ErrorResponse fail(final ErrorType errorType, final BindingResult bindingResult) {
        return new ErrorResponse(errorType.getCode(), errorType.getMessage(), ValidationError.of(bindingResult));
    }

    @JsonInclude(Include.NON_NULL)
    private record ValidationError(
            String path,
            String field,
            String message
    ) {

        // ConstraintViolation 목록을 ValidationError로 변환
        private static List<ValidationError> of(final Set<ConstraintViolation<?>> violations) {
            return violations.stream()
                    .map(violation -> new ValidationError(
                            violation.getPropertyPath().toString(),
                            null,
                            violation.getMessage()
                    ))
                    .toList();
        }

        // BindingResult의 FieldError 목록을 ValidationError로 변환
        private static List<ValidationError> of(final BindingResult bindingResult) {
            return bindingResult.getFieldErrors().stream()
                    .map(error -> new ValidationError(null, error.getField(), error.getDefaultMessage()))
                    .toList();
        }
    }
}
