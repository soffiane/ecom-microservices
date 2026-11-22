package com.ecommerce.order.config;

import com.ecommerce.order.clients.ProductClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ProductClientConfig {

    // builder par défaut, non load-balancé : utilisé notamment par le client Eureka pour s'enregistrer.
    // Sans lui, Eureka prendrait le builder load-balancé et chercherait un service nommé "localhost".
    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    // builder load-balancé (résolution des noms Eureka), réservé à ProductClient et UserClient
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public ProductClient productClient(@LoadBalanced RestClient.Builder builder) {
        RestClient restClient = builder
                .baseUrl("http://PRODUCT-SERVICE") // nom du service dans Eureka
                .build();
        RestClientAdapter restClientAdapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(restClientAdapter)
                .build();
        return httpServiceProxyFactory.createClient(ProductClient.class);
    }
}
