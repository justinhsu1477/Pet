package com.pet.exception;

import com.pet.dto.response.ErrorResponse;
import com.pet.dto.response.FieldErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                errorCode.getCode(),
                "Not Found",
                ex.getMessage(),
                errorCode.getUserMessage(),
                request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<FieldErrorDto> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                String code = determineFieldErrorCode(fieldError);
                fieldErrors.add(new FieldErrorDto(
                        fieldError.getField(),
                        code,
                        fieldError.getDefaultMessage(),
                        fieldError.getRejectedValue()));
            }
        });

        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                errorCode.getCode(),
                "Validation Failed",
                "輸入資料驗證失敗",
                errorCode.getUserMessage(),
                request.getRequestURI(),
                fieldErrors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                errorCode.getCode(),
                "Internal Server Error",
                "伺服器內部錯誤: " + ex.getMessage(),
                errorCode.getUserMessage(),
                request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 輔助方法: 決定欄位錯誤代碼
    private String determineFieldErrorCode(FieldError error) {
        String code = error.getCode();
        if (code == null) {
            return ErrorCode.INVALID_INPUT.getCode();
        }

        return switch (code) {
            case "NotBlank", "NotNull", "NotEmpty" -> ErrorCode.FIELD_REQUIRED.getCode();
            case "Size", "Length" -> ErrorCode.FIELD_TOO_LONG.getCode();
            case "Min", "Max", "Range" -> ErrorCode.FIELD_OUT_OF_RANGE.getCode();
            case "Pattern", "Email" -> ErrorCode.FIELD_INVALID_FORMAT.getCode();
            default -> ErrorCode.INVALID_INPUT.getCode();
        };
    }
}
