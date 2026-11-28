package com.ecommerce.gateway.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// Routes lues dans le gateway.yml du config-server (sans config-server démarré) : le test échoue si les routes
// YAML sont commentées ou si une réécriture est fausse. GatewayConfig (routes Java) est désactivé.
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.config.import=file:../config-server/src/main/resources/config/gateway.yml"
})
class GatewayConfigTest {

    @Autowired
    private RouteLocator routeLocator;

    /** Simule le passage dans la gateway : première route dont le prédicat accepte la requête, puis ses filtres. */
    private String[] forward(String pathAndQuery) {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get(pathAndQuery));
        Route route = routeLocator.getRoutes()
                .filterWhen(r -> Mono.from(r.getPredicate().apply(exchange)))
                .blockFirst();
        if (route == null) {
            return null;
        }
        AtomicReference<String> forwarded = new AtomicReference<>(exchange.getRequest().getURI().getRawPath()
                + (exchange.getRequest().getURI().getRawQuery() == null ? "" : "?" + exchange.getRequest().getURI().getRawQuery()));
        for (GatewayFilter filter : route.getFilters()) {
            filter.filter(exchange, ex -> {
                String query = ex.getRequest().getURI().getRawQuery();
                forwarded.set(ex.getRequest().getURI().getRawPath() + (query == null ? "" : "?" + query));
                return Mono.empty();
            }).block();
        }
        return new String[]{route.getId(), forwarded.get()};
    }

    @ParameterizedTest
    @CsvSource({
            "/products,                product-service, /api/v1/products",
            "/products/,               product-service, /api/v1/products/",
            "/products/12,             product-service, /api/v1/products/12",
            "/products/search?name=tv, product-service, /api/v1/products/search?name=tv",
            "/users,                   user-service,    /api/v1/users",
            "/users/6b0ae07bf894a41c5e3f7533, user-service, /api/v1/users/6b0ae07bf894a41c5e3f7533",
            "/orders,                  order-service,   /api/v1/orders",
            "/cart,                    order-service,   /api/v1/cart",
            "/cart/items,              order-service,   /api/v1/cart/items",
            "/cart/items/15,           order-service,   /api/v1/cart/items/15"
    })
    void route_shouldRewritePublicPathToServiceApiPath(String path, String expectedRoute, String expectedForwardedPath) {
        //when
        String[] result = forward(path);

        //then
        assertEquals(expectedRoute, result[0]);
        assertEquals(expectedForwardedPath, result[1]);
    }

    @Test
    void route_shouldNotMatchWhenPathOnlyStartsWithServicePrefix() {
        //when
        String[] result = forward("/productsX");

        //then
        assertNull(result);
    }
}
