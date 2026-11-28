package com.ecommerce.gateway.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.observation.ClientRequestObservationContext;

@Configuration
public class ObservationConfig {

    // Exclut du tracing les appels au serveur Eureka (heartbeat et récupération du registre toutes les 30 s).
    // Le client Eureka utilise RestClient (WebClient seulement si eureka.client.webclient.enabled=true).
    @Bean
    public ObservationPredicate noEurekaObservations() {
        return (name, context) -> !(context instanceof ClientRequestObservationContext clientContext
                && clientContext.getCarrier() != null
                && clientContext.getCarrier().getURI().getPath().startsWith("/eureka/"));
    }
}
