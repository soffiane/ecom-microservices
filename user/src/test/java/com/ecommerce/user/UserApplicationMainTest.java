package com.ecommerce.user;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

/**
 * Covers main() without starting the application: SpringApplication.run is stubbed.
 * The full context startup is covered by UserApplicationTests.
 */
class UserApplicationMainTest {

    @Test
    void main_shouldStartSpringApplicationWithArgs() {
        //given
        String[] args = {"--server.port=0"};
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {

            //when
            UserApplication.main(args);

            //then
            springApplication.verify(() -> SpringApplication.run(UserApplication.class, args));
        }
    }
}
