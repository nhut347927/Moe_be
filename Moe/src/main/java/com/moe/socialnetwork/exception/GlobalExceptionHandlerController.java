package com.moe.socialnetwork.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.moe.socialnetwork.common.response.ResponseAPI;

@ControllerAdvice
public class GlobalExceptionHandlerController {

	// Handle validation errors (Spring validation @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseAPI<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest()
                .body(ResponseAPI.error(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors));
    }

	// Handle custom AppException with customizable error code
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ResponseAPI<Void>> handleAppException(AppException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(ResponseAPI.error(ex.getStatusCode(), ex.getMessage(), null));
    }

	// Handle all unexpected errors (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseAPI<Void>> handleGlobalException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseAPI.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred: ", null));
    }
}
