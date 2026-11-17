package com.ecommerce.order.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({OutOfStockException.class, EmptyCartException.class, CartItemNotFoundException.class})
    public ResponseEntity<String> handleBadRequestExceptions(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    /**
     * Raised e.g. when two concurrent requests add the same product to the same cart
     * and hit the (user_id, product_id) unique constraint. The client can safely retry.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Concurrent modification, please retry");
    }
}
