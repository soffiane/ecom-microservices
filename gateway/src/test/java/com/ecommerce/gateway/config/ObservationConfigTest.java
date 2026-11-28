package com.ecommerce.gateway.config;

import io.micrometer.observation.ObservationPredicate;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.mock.http.client.MockClientHttpRequest;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObservationConfigTest {

    private final ObservationPredicate predicate = new ObservationConfig().noEurekaObservations();

    private static ClientRequestObservationContext clientRequest(String uri) {
        return new ClientRequestObservationContext(new MockClientHttpRequest(HttpMethod.GET, URI.create(uri)));
    }

    @Test
    void noEurekaObservations_shouldSkipWhenClientCallTargetsEureka() {
        //given - heartbeat / registry fetch of the Eureka client, every 30 s
        ClientRequestObservationContext context = clientRequest("http://localhost:8761/eureka/apps/delta");

        //when
        boolean observed = predicate.test("http.client.requests", context);

        //then
        assertFalse(observed);
    }

    @Test
    void noEurekaObservations_shouldKeepWhenClientCallTargetsAnotherService() {
        //given
        ClientRequestObservationContext context = clientRequest("http://localhost:8083/api/v1/cart/items");

        //when
        boolean observed = predicate.test("http.client.requests", context);

        //then
        assertTrue(observed);
    }
}
