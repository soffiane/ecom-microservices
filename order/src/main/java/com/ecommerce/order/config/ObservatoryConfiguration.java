package com.ecommerce.order.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class ObservatoryConfiguration {

    private final ObservationRegistry observationRegistry;

    private final Tracer tracer;

    private final Propagator propagator;

    public ObservatoryConfiguration(ObservationRegistry observationRegistry, Tracer tracer, Propagator propagator) {
        this.observationRegistry = observationRegistry;
        this.tracer = tracer;
        this.propagator = propagator;
    }

    // builder par défaut, non load-balancé : utilisé notamment par le client Eureka pour s'enregistrer.
    // Sans lui, Eureka prendrait le builder load-balancé et chercherait un service nommé "localhost".
    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        RestClient.Builder builder = RestClient.builder();
        if(observationRegistry != null){
            builder.requestInterceptor(createTracingInterceptor());
        }
        return builder;
    }

    private ClientHttpRequestInterceptor createTracingInterceptor() {
        return ((request, body, execution) -> {
            if(tracer != null && propagator != null && tracer.currentSpan() != null){
                TraceContext context = tracer.currentTraceContext().context();
                if (context != null) {
                    propagator.inject(context,
                            request.getHeaders(),
                            (carrier, key, value) -> carrier.add(key, value));
                }
            }
            return execution.execute(request, body);
        });
    }

    // builder load-balancé (résolution des noms Eureka), réservé à ProductClient et UserClient.
    // observationRegistry : chaque appel devient un span enfant de la requête en cours et les en-têtes
    // de trace sont propagés, donc order → product/user apparaissent dans la même trace.
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder().observationRegistry(observationRegistry);
    }


}
