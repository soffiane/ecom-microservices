package com.ecommerce.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

//@Configuration
public class GatewayConfig {

    //@Bean
    public RouteLocator customROuteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // /xxx et /xxx/... → /api/v1/xxx... : le "/" reste dans le segment capturé, sinon /xxx n'est pas
                // réécrit (403 via /error côté service) et /xxx/12 devient /api/v1/xxx12
                .route("product-service", r -> r.path("/api/v1/products/**")
                        //.filters(f -> f.rewritePath("/products(?<segment>/?.*)", "/api/v1/products${segment}"))
                        .uri("lb://PRODUCT-SERVICE"))
                .route("user-service", r -> r.path("/api/v1/users/**")
                        //.filters(f -> f.rewritePath("/users(?<segment>/?.*)", "/api/v1/users${segment}"))
                        .uri("lb://USER-SERVICE"))
                .route("order-service", r -> r.path("/api/v1/orders/**", "/api/v1/cart/**")
                        //.filters(f -> f.rewritePath("/(?<resource>orders|cart)(?<segment>/?.*)", "/api/v1/${resource}${segment}"))
                        .uri("lb://ORDER-SERVICE"))
                .route("eureka-service", r -> r.path("/eureka/main").filters(p -> p.setPath("/"))
                        .uri("http://localhost:8761"))
                .route("eureka-server-static", r -> r.path("/eureka/**")
                        .uri("http://localhost:8761"))
                .build();
    }
}
