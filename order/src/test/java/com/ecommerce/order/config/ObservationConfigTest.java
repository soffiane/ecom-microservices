package com.ecommerce.order.config;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObservationConfigTest {

    private final ObservationPredicate predicate = new ObservationConfig().noActuatorObservations();

    private static ServerRequestObservationContext request(String uri) {
        return new ServerRequestObservationContext(new MockHttpServletRequest("GET", uri), new MockHttpServletResponse());
    }

    private static ClientRequestObservationContext clientRequest(String uri) {
        return new ClientRequestObservationContext(new MockClientHttpRequest(HttpMethod.GET, URI.create(uri)));
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
        // registre avec un handler : sans handler, Micrometer renvoie une observation noop
        ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(ctx -> true);
        context.setParentObservation(Observation.start("http.server.requests", registry));

        //when
        boolean observed = predicate.test("spring.security.filterchains", context);

        //then
        assertTrue(observed);
    }

    @Test
    void noActuatorObservations_shouldSkipWhenSecurityObservationParentIsNoop() {
        //given - a skipped actuator request still leaves a noop observation in scope
        Observation.Context context = new Observation.Context();
        context.setParentObservation(Observation.NOOP);

        //when
        boolean observed = predicate.test("spring.security.filterchains", context);

        //then
        assertFalse(observed);
    }

    @Test
    void noActuatorObservations_shouldSkipWhenClientCallTargetsEureka() {
        //given - heartbeat / registry fetch of the Eureka client, every 30 s
        ClientRequestObservationContext context = clientRequest("http://localhost:8761/eureka/apps/delta");

        //when
        boolean observed = predicate.test("http.client.requests", context);

        //then
        assertFalse(observed);
    }

    @Test
    void noActuatorObservations_shouldKeepWhenClientCallTargetsAnotherService() {
        //given
        ClientRequestObservationContext context = clientRequest("http://localhost:8081/api/v1/products/1");

        //when
        boolean observed = predicate.test("http.client.requests", context);

        //then
        assertTrue(observed);
    }
}
