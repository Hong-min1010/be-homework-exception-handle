package com.springboot.response;

import com.springboot.exception.BusinessLogicException;
import lombok.Getter;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class ErrorResponse {
    private Integer status;
    private String message;
    private List<FieldError> fieldErrors;
    private List<ConstraintViolationError> violationErrors;


    private ErrorResponse(final Integer status, String message,
            final List<FieldError> fieldErrors,
            final List<ConstraintViolationError> violationErrors) {
        this.fieldErrors = fieldErrors;
        this.violationErrors = violationErrors;
        this.status = status;
        this.message = message;
    }

    // ErrorResponse 에 있는 of 메서드는 GlobalExceptionAdvice 에서 가져온 of이다.
    // GlobalException에서 오류가 어떤 형태일지 모르기 때문에

    public static ErrorResponse of(BindingResult bindingResult) {
        return new ErrorResponse(null, null, FieldError.of(bindingResult), null);
    }

    public static ErrorResponse of(Set<ConstraintViolation<?>> violations) {
        return new ErrorResponse(null, null,null, ConstraintViolationError.of(violations));
    }

    public static ErrorResponse of(BusinessLogicException businessLogicException) {
        Integer status = businessLogicException.getExceptionCode().getStatus();
        String message = businessLogicException.getMessage();
        return new ErrorResponse(status, message, null, null);
    }

    public static ErrorResponse of(HttpRequestMethodNotSupportedException e) {
//        Integer status = HttpRequestMethodNotSupportedException.getExceptionCode().getStatus();
//        String message = businessLogicException.getMessage();
        return new ErrorResponse(405, "Method Not Allowed", null, null);
    }

    public static ErrorResponse of(NullPointerException n) {

        return new ErrorResponse(500, "Internal Server Error", null, null);
    }

    @Getter
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String reason;

        private FieldError(String field, Object rejectedValue, String reason) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.reason = reason;
        }


        public static List<FieldError> of(BindingResult bindingResult) {
            final List<org.springframework.validation.FieldError> fieldErrors =
                                                        bindingResult.getFieldErrors();
            return fieldErrors.stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getRejectedValue() == null ?
                                            "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()))
                    .collect(Collectors.toList());
        }

    }

    @Getter
    public static class ConstraintViolationError {
        private String propertyPath;
        private Object rejectedValue;
        private String reason;

        private ConstraintViolationError(String propertyPath, Object rejectedValue,
                                   String reason) {
            this.propertyPath = propertyPath;
            this.rejectedValue = rejectedValue;
            this.reason = reason;
        }

        public static List<ConstraintViolationError> of(
                Set<ConstraintViolation<?>> constraintViolations) {
            return constraintViolations.stream()
                    .map(constraintViolation -> new ConstraintViolationError(
                            constraintViolation.getPropertyPath().toString(),
                            constraintViolation.getInvalidValue().toString(),
                            constraintViolation.getMessage()
                    )).collect(Collectors.toList());
        }
    }

}