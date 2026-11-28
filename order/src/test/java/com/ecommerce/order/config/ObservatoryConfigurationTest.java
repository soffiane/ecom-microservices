package com.ecommerce.order.config;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ObservatoryConfigurationTest {

    @Test
    void loadBalancedRestClientBuilder_shouldObserveCallsAsChildOfCurrentObservation() {
        //given
        List<Observation.Context> started = new ArrayList<>();
        ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(new ObservationHandler<>() {
            @Override
            public void onStart(Observation.Context context) {
                started.add(context);
            }

            @Override
            public boolean supportsContext(Observation.Context context) {
                return true;
            }
        });
        RestClient.Builder builder = new ObservatoryConfiguration(registry, Tracer.NOOP, Propagator.NOOP)
                .loadBalancedRestClientBuilder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://PRODUCT-SERVICE/api/v1/products/1")).andRespond(withSuccess());
        RestClient restClient = builder.build();
        Observation parent = Observation.start("http.server.requests", registry);

        //when
        parent.observe(() -> restClient.get().uri("http://PRODUCT-SERVICE/api/v1/products/1").retrieve().toBodilessEntity());

        //then
        server.verify();
        List<Observation.Context> clientCalls = started.stream()
                .filter(context -> "http.client.requests".equals(context.getName()))
                .toList();
        assertEquals(1, clientCalls.size());
        assertSame(parent, clientCalls.get(0).getParentObservation());
    }
}
