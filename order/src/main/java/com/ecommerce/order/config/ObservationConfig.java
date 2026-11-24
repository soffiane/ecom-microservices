package com.ecommerce.order.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
public class ObservationConfig {

    // Exclut /actuator/** du tracing (Prometheus scrape /actuator/prometheus toutes les 3 s et noie Zipkin).
    // Effet de bord : http.server.requests n'a plus de métrique pour /actuator/**.
    @Bean
    public ObservationPredicate noActuatorObservations() {
        return (name, context) -> {
            if (context instanceof ServerRequestObservationContext serverContext) {
                return !serverContext.getCarrier().getRequestURI().startsWith("/actuator");
            }
            // sans observation HTTP parente (requête actuator exclue), les observations Spring Security
            // démarreraient chacune leur propre trace
            return !(name.startsWith("spring.security.") && context.getParentObservation() == null);
        };
    }
}
