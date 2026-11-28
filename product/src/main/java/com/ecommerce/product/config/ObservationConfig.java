package com.ecommerce.product.config;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
public class ObservationConfig {

    // Exclut /actuator/** et les appels au serveur Eureka du tracing (Prometheus scrape /actuator/prometheus toutes les 3 s et noie Zipkin).
    // Effet de bord : http.server.requests n'a plus de métrique pour /actuator/**.
    @Bean
    public ObservationPredicate noActuatorObservations() {
        return (name, context) -> {
            if (context instanceof ServerRequestObservationContext serverContext) {
                return !serverContext.getCarrier().getRequestURI().startsWith("/actuator");
            }
            // heartbeat et récupération du registre par le client Eureka (toutes les 30 s) : une trace racine par appel
            if (context instanceof ClientRequestObservationContext clientContext && clientContext.getCarrier() != null) {
                return !clientContext.getCarrier().getURI().getPath().startsWith("/eureka/");
            }
            // sans observation HTTP parente (requête actuator exclue), les observations Spring Security
            // démarreraient chacune leur propre trace. Une observation exclue reste en scope sous forme
            // de noop : le parent est alors ce noop, pas null.
            ObservationView parent = context.getParentObservation();
            boolean noParent = parent == null || (parent instanceof Observation observation && observation.isNoop());
            return !(name.startsWith("spring.security.") && noParent);
        };
    }
}
