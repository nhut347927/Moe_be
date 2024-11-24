package com.moe.music.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.moe.music.response.ResponseAPI;

@ControllerAdvice
public class GlobalExceptionHandlerController {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {

		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		List<String> errorMessages = errors.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue())
				.collect(Collectors.toList());

		ResponseAPI<Void> response = ResponseAPI.error(HttpStatus.BAD_REQUEST.value(), "Validation failed",
				errorMessages);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}
}
