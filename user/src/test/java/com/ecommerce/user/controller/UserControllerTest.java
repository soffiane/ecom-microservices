package com.ecommerce.user.controller;

import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.mapper.UserRequestMapper;
import com.ecommerce.user.mapper.UserResponseMapper;
import com.ecommerce.user.model.User;
import com.ecommerce.user.model.UserRole;
import com.ecommerce.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRequestMapper userRequestMapper;

    @MockitoBean
    private UserResponseMapper userResponseMapper;

    @Test
    void shouldGetAllUsers() throws Exception {
        //given
        User user1 = User.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build();

        User user2 = User.builder()
                .id("2")
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("0987654321")
                .role(UserRole.CUSTOMER)
                .build();
        //when
        when(userService.findAll()).thenReturn(List.of(user1, user2));
        when(userResponseMapper.mapToUserResponse(user1)).thenReturn(UserResponse.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build());
        when(userResponseMapper.mapToUserResponse(user2)).thenReturn(UserResponse.builder()
                .id("2")
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("0987654321")
                .role(UserRole.CUSTOMER)
                .build());
        //then
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[1].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$[1].email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$[0].phone").value("1234567890"))
                .andExpect(jsonPath("$[1].phone").value("0987654321"))
                .andExpect(jsonPath("$[0].role").value("CUSTOMER"))
                .andExpect(jsonPath("$[1].role").value("CUSTOMER"))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void shouldCreateUser() throws Exception {
        //given
        UserResponse userResponse = UserResponse.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build();

        when(userService.addUser(any(UserRequest.class))).thenReturn(userResponse);

        //when & then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\",\"phone\":\"1234567890\",\"role\":\"CUSTOMER\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phone").value("1234567890"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));

        verify(userService).addUser(any(UserRequest.class));
    }

    @Test
    void shouldGetUserById() throws Exception {
        //given
        User user = User.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build();

        when(userService.findById("1")).thenReturn(java.util.Optional.of(user));
        when(userResponseMapper.mapToUserResponse(user)).thenReturn(userResponse);

        //when & then
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phone").value("1234567890"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void shouldReturnNotFoundWhenUserNotExists() throws Exception {
        //given
        when(userService.findById("999")).thenReturn(java.util.Optional.empty());

        //when & then
        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateUser() throws Exception {
        //given
        UserRequest userRequest = UserRequest.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build();

        User updatedUser = User.builder()
                .id("1")
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id("1")
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build();

        when(userService.updateUser("1", userRequest)).thenReturn(java.util.Optional.of(updatedUser));
        when(userResponseMapper.mapToUserResponse(updatedUser)).thenReturn(userResponse);

        //when & then
        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Smith\",\"email\":\"john.smith@example.com\",\"phone\":\"1234567890\",\"role\":\"CUSTOMER\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("john.smith@example.com"))
                .andExpect(jsonPath("$.phone").value("1234567890"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentUser() throws Exception {
        //given
        UserRequest userRequest = UserRequest.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build();

        when(userService.updateUser("999", userRequest)).thenReturn(java.util.Optional.empty());

        //when & then
        mockMvc.perform(put("/api/v1/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Smith\",\"email\":\"john.smith@example.com\",\"phone\":\"1234567890\",\"role\":\"CUSTOMER\"}"))
                .andExpect(status().isNotFound());
    }
}
