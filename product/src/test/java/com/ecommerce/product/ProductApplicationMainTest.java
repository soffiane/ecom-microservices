package com.ecommerce.product;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

/**
 * Covers main() without starting the application: SpringApplication.run is stubbed.
 * The full context startup is covered by ProductApplicationTests.
 */
class ProductApplicationMainTest {

    @Test
    void main_shouldStartSpringApplicationWithArgs() {
        //given
        String[] args = {"--server.port=0"};
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {

            //when
            ProductApplication.main(args);

            //then
            springApplication.verify(() -> SpringApplication.run(ProductApplication.class, args));
        }
    }
}
