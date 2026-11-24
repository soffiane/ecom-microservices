package com.ecommerce.order.config;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObservationConfigTest {

    private final ObservationPredicate predicate = new ObservationConfig().noActuatorObservations();

    private static ServerRequestObservationContext request(String uri) {
        return new ServerRequestObservationContext(new MockHttpServletRequest("GET", uri), new MockHttpServletResponse());
    }

    @Test
    void noActuatorObservations_shouldSkipWhenRequestTargetsActuator() {
        //given
        ServerRequestObservationContext context = request("/actuator/prometheus");

        //when
        boolean observed = predicate.test("http.server.requests", context);

        //then
        assertFalse(observed);
    }

    @Test
    void noActuatorObservations_shouldKeepWhenRequestTargetsApi() {
        //given
        ServerRequestObservationContext context = request("/api/v1/cart");

        //when
        boolean observed = predicate.test("http.server.requests", context);

        //then
        assertTrue(observed);
    }

    @Test
    void noActuatorObservations_shouldSkipWhenSecurityObservationHasNoParent() {
        //given - parent HTTP observation skipped (actuator): security would start its own trace
        Observation.Context context = new Observation.Context();

        //when
        boolean observed = predicate.test("spring.security.filterchains", context);

        //then
        assertFalse(observed);
    }

    @Test
    void noActuatorObservations_shouldKeepWhenSecurityObservationHasParent() {
        //given
        Observation.Context context = new Observation.Context();
        context.setParentObservation(Observation.start("http.server.requests", ObservationRegistry.create()));

        //when
        boolean observed = predicate.test("spring.security.filterchains", context);

        //then
        assertTrue(observed);
    }
}
