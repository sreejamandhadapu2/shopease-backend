package com.shopease.backend.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<Map<String,Object>> handleProductNotFound(ProductNotFoundException ex,HttpServletRequest request){
		Map<String,Object> body = new HashMap<>();
		body.put("status", 404);
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	
}
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String,Object>> handleValidationErrors(MethodArgumentNotValidException ex){
		Map<String,Object> body= new HashMap<>();
		body.put("Status", 400);
		body.put("error", "Bad Request");
		Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        body.put("message", errors);
        return ResponseEntity.badRequest().body(body);
		
	}
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String,Object>> handleValidId(MethodArgumentTypeMismatchException ex){
		Map<String,Object> body = new HashMap<>();
		 body.put("status", 400);
	        body.put("error", "Bad Request");
	        body.put("message", "Product ID must be a valid number");

	        return ResponseEntity.badRequest().body(body);
	}
	@ExceptionHandler(CategoryNotFoundException.class)
	public ResponseEntity<Map<String,Object>> handleCategoryNotFound(CategoryNotFoundException ex,HttpServletRequest request){
		Map<String,Object> body = new HashMap<>();
		body.put("status", 404);
	    body.put("error", "Not Found");
	    body.put("message", ex.getMessage());
	    body.put("path", request.getRequestURI());

	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}
	@ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmail(
            EmailAlreadyExistsException ex) {

        Map<String, String> body = new HashMap<>();
        body.put("error", "Email Already Exists");
        body.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }
	@ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(
            InvalidCredentialsException ex) {

        Map<String, String> body = new HashMap<>();
        body.put("error", "Unauthorized");
        body.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }

@ExceptionHandler(IllegalStateException.class)
public ResponseEntity<Map<String, Object>> handleIllegalState(
        IllegalStateException ex,
        HttpServletRequest request) {

    Map<String, Object> body = new HashMap<>();
    body.put("status", 409);
    body.put("error", "Conflict");
    body.put("message", ex.getMessage());
    body.put("path", request.getRequestURI());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
}

@ExceptionHandler(OptimisticLockingFailureException.class)
public ResponseEntity<Map<String, Object>> handleOptimisticLock(
        OptimisticLockingFailureException ex,
        HttpServletRequest request) {

    Map<String, Object> body = new HashMap<>();
    body.put("status", 409);
    body.put("error", "Conflict");
    body.put("message",
            "Stock changed during checkout. Please try again.");
    body.put("path", request.getRequestURI());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
}

}
