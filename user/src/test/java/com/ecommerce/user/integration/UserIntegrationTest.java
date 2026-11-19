package com.ecommerce.user.integration;

import com.ecommerce.user.model.User;
import com.ecommerce.user.model.UserRole;
import com.ecommerce.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class UserIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void shouldCreateAndRetrieveUser() throws Exception {
        //given
        String userJson = """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "john.doe@example.com",
                    "phone": "1234567890",
                    "role": "CUSTOMER"
                }
                """;

        //when - create user
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        //then - verify user was saved
        User savedUser = userRepository.findAll().getFirst();
        assertEquals("John", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
    }

    @Test
    @WithMockUser
    void shouldGetAllUsers() throws Exception {
        //given
        User user1 = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build();

        User user2 = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phone("0987654321")
                .role(UserRole.CUSTOMER)
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        //when & then
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser
    void shouldGetUserById() throws Exception {
        //given
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build();
        User savedUser = userRepository.save(user);

        //when & then
        mockMvc.perform(get("/api/v1/users/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundForNonExistentUser() throws Exception {
        //when & then
        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldUpdateUser() throws Exception {
        //given
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build();
        User savedUser = userRepository.save(user);

        String updateJson = """
                {
                    "firstName": "John",
                    "lastName": "Smith",
                    "email": "john.smith@example.com",
                    "phone": "1234567890",
                    "role": "CUSTOMER"
                }
                """;

        //when & then
        mockMvc.perform(put("/api/v1/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("john.smith@example.com"));

        //verify in database
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertEquals("Smith", updatedUser.getLastName());
        assertEquals("john.smith@example.com", updatedUser.getEmail());
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenUpdatingNonExistentUser() throws Exception {
        //given
        String updateJson = """
                {
                    "firstName": "John",
                    "lastName": "Smith",
                    "email": "john.smith@example.com",
                    "phone": "1234567890",
                    "role": "CUSTOMER"
                }
                """;

        //when & then
        mockMvc.perform(put("/api/v1/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldReturnConflictWhenCreatingUserWithExistingEmail() throws Exception {
        //given
        userRepository.save(User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build());

        String duplicateJson = """
                {
                    "firstName": "Johnny",
                    "lastName": "Other",
                    "email": "john.doe@example.com",
                    "phone": "0987654321",
                    "role": "CUSTOMER"
                }
                """;

        //when & then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already exists"));

        assertEquals(1, userRepository.count());
    }

    @Test
    @WithMockUser
    void shouldReturnConflictWhenUpdatingUserToExistingEmail() throws Exception {
        //given
        userRepository.save(User.builder()
                .firstName("John").lastName("Doe").email("john.doe@example.com")
                .phone("1234567890").role(UserRole.CUSTOMER).build());
        User jane = userRepository.save(User.builder()
                .firstName("Jane").lastName("Doe").email("jane.doe@example.com")
                .phone("0987654321").role(UserRole.CUSTOMER).build());

        String updateJson = """
                {
                    "firstName": "Jane",
                    "lastName": "Doe",
                    "email": "john.doe@example.com",
                    "phone": "0987654321",
                    "role": "CUSTOMER"
                }
                """;

        //when & then
        mockMvc.perform(put("/api/v1/users/" + jane.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isConflict());

        assertEquals("jane.doe@example.com", userRepository.findById(jane.getId()).orElseThrow().getEmail());
    }

    @Test
    void apiDocs_shouldBeServedWithoutAuthenticationWhenRequested() throws Exception {
        //given - no authenticated user

        //when / then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("User Service API"))
                .andExpect(jsonPath("$.paths['/api/v1/users/{id}']").exists());
    }
}
