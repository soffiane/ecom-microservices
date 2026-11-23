package com.ecommerce.product.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditorAwareImplTest {

    private final AuditorAwareImpl auditorAware = new AuditorAwareImpl();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAuditor_shouldReturnUserNameWhenAuthenticated() {
        //given
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("alice", null, List.of()));

        //when
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        //then
        assertEquals(Optional.of("alice"), auditor);
    }

    @Test
    void getCurrentAuditor_shouldReturnSystemWhenNoAuthentication() {
        //when
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        //then
        assertEquals(Optional.of("system"), auditor);
    }

    @Test
    void getCurrentAuditor_shouldReturnSystemWhenNotAuthenticated() {
        //given
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.unauthenticated("alice", "secret"));

        //when
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        //then
        assertEquals(Optional.of("system"), auditor);
    }

    @Test
    void getCurrentAuditor_shouldReturnSystemWhenAnonymousUser() {
        //given
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("anonymousUser", null, List.of()));

        //when
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        //then
        assertEquals(Optional.of("system"), auditor);
    }
}
