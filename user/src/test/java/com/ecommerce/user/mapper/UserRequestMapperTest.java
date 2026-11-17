package com.ecommerce.user.mapper;

import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.model.User;
import com.ecommerce.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRequestMapperTest {

    private UserRequestMapper userRequestMapper;

    @BeforeEach
    void setUp() {
        userRequestMapper = new UserRequestMapper();
    }

    @Test
    void mapToUserRequest_shouldMapAllFields() {
        //given
        AddressDTO addressDTO = AddressDTO.builder()
                .id("1")
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .zipcode("10001")
                .country("USA")
                .build();

        UserRequest userRequest = UserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .address(addressDTO)
                .build();

        //when
        User result = userRequestMapper.mapToUserRequest(userRequest);

        //then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("1234567890", result.getPhone());
        assertEquals(UserRole.CUSTOMER, result.getRole());
        assertNotNull(result.getAddress());
        assertEquals("123 Main St", result.getAddress().getStreet());
        assertEquals("New York", result.getAddress().getCity());
        assertEquals("NY", result.getAddress().getState());
        assertEquals("10001", result.getAddress().getZipCode());
        assertEquals("USA", result.getAddress().getCountry());
    }

    @Test
    void mapToUserRequest_shouldHandleNullAddress() {
        //given
        UserRequest userRequest = UserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .address(null)
                .build();

        //when
        User result = userRequestMapper.mapToUserRequest(userRequest);

        //then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertNotNull(result.getAddress());
    }

    @Test
    void mapToUserRequest_shouldHandleEmptyAddressDTO() {
        //given
        AddressDTO addressDTO = new AddressDTO();

        UserRequest userRequest = UserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .address(addressDTO)
                .build();

        //when
        User result = userRequestMapper.mapToUserRequest(userRequest);

        //then
        assertNotNull(result);
        assertNotNull(result.getAddress());
    }
}
