package com.ecommerce.user.service;

import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.mapper.UserRequestMapper;
import com.ecommerce.user.mapper.UserResponseMapper;
import com.ecommerce.user.model.User;
import com.ecommerce.user.model.UserRole;
import com.ecommerce.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserResponseMapper userResponseMapper;

    @Mock
    private UserRequestMapper userRequestMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build();

        userRequest = UserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build();

        userResponse = UserResponse.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .build();
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        //given
        User user2 = User.builder()
                .id("1")
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("0987654321")
                .role(UserRole.CUSTOMER)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        //when
        List<User> result = userService.findAll();

        //then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());
        verify(userRepository).findAll();
    }

    @Test
    void addUser_shouldReturnUserResponse() {
        //given
        when(userRequestMapper.mapToUserRequest(userRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userResponseMapper.mapToUserResponse(user)).thenReturn(userResponse);

        //when
        UserResponse result = userService.addUser(userRequest);

        //then
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(userRequestMapper).mapToUserRequest(userRequest);
        verify(userRepository).save(user);
        verify(userResponseMapper).mapToUserResponse(user);
    }

    @Test
    void findById_shouldReturnUserWhenExists() {
        //given
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        //when
        Optional<User> result = userService.findById("1");

        //then
        assertTrue(result.isPresent());
        assertEquals("1", result.get().getId());
        assertEquals("John", result.get().getFirstName());
        verify(userRepository).findById("1");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        //given
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        //when
        Optional<User> result = userService.findById("999");

        //then
        assertFalse(result.isPresent());
        verify(userRepository).findById("999");
    }

    @Test
    void updateUser_shouldReturnUpdatedUserWhenExists() {
        //given
        UserRequest updateRequest = UserRequest.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build();

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        Optional<User> result = userService.updateUser("1", updateRequest);

        //then
        assertTrue(result.isPresent());
        assertEquals("Smith", result.get().getLastName());
        assertEquals("john.smith@example.com", result.get().getEmail());
        verify(userRepository).findById("1");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_shouldReturnEmptyWhenNotExists() {
        //given
        UserRequest updateRequest = UserRequest.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .build();

        when(userRepository.findById("999")).thenReturn(Optional.empty());

        //when
        Optional<User> result = userService.updateUser("999", updateRequest);

        //then
        assertFalse(result.isPresent());
        verify(userRepository).findById("999");
        verify(userRepository, never()).save(any(User.class));
    }
}
