package com.ecommerce.user.config;

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
    void getCurrentAuditor_shouldReturnEmptyWhenNoAuthentication() {
        //when
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        //then
        assertEquals(Optional.empty(), auditor);
    }

    @Test
    void getCurrentAuditor_shouldReturnEmptyWhenNotAuthenticated() {
        //given
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.unauthenticated("alice", "secret"));

        //when
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        //then
        assertEquals(Optional.empty(), auditor);
    }

    @Test
    void getCurrentAuditor_shouldReturnEmptyWhenAnonymousUser() {
        //given
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("anonymousUser", null, List.of()));

        //when
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        //then
        assertEquals(Optional.empty(), auditor);
    }
}
