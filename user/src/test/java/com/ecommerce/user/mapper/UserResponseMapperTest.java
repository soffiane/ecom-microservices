package com.ecommerce.user.mapper;

import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.model.Address;
import com.ecommerce.user.model.User;
import com.ecommerce.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserResponseMapperTest {

    private UserResponseMapper userResponseMapper;

    @BeforeEach
    void setUp() {
        userResponseMapper = new UserResponseMapper();
    }

    @Test
    void mapToUserResponse_shouldMapAllFields() {
        //given
        Address address = Address.builder()
                .id("1")
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .country("USA")
                .build();

        User user = User.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .address(address)
                .build();

        //when
        UserResponse result = userResponseMapper.mapToUserResponse(user);

        //then
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("1234567890", result.getPhone());
        assertNotNull(result.getAddress());
        assertEquals("123 Main St", result.getAddress().getStreet());
        assertEquals("New York", result.getAddress().getCity());
        assertEquals("NY", result.getAddress().getState());
        assertEquals("10001", result.getAddress().getZipcode());
        assertEquals("USA", result.getAddress().getCountry());
    }

    @Test
    void mapToUserResponse_shouldHandleNullAddress() {
        //given
        User user = User.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .address(null)
                .build();

        //when
        UserResponse result = userResponseMapper.mapToUserResponse(user);

        //then
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("John", result.getFirstName());
        assertNotNull(result.getAddress());
    }

    @Test
    void mapToUserResponse_shouldHandleEmptyAddress() {
        //given
        Address address = new Address();

        User user = User.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .address(address)
                .build();

        //when
        UserResponse result = userResponseMapper.mapToUserResponse(user);

        //then
        assertNotNull(result);
        assertNotNull(result.getAddress());
    }
}
