package com.java.todolist.application.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ValidationErrorHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();

        StringBuilder description = new StringBuilder();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            description.append(fieldError.getDefaultMessage())
                       .append("; ");
        }

        if (description.length() > 0) {
            description.setLength(description.length() - 2);
        }

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), description.toString());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
