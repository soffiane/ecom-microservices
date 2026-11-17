package com.ecommerce.user.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    @Test
    void shouldReturnNotFoundWhenUserNotFoundExceptionThrown() {
        //given
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        UserNotFoundException exception = new UserNotFoundException("User not found");

        //when
        ResponseEntity<String> response = handler.handleUserNotFoundException(exception);

        //then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}
