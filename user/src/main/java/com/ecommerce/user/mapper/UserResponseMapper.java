package com.ecommerce.user.mapper;


import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.model.Address;
import com.ecommerce.user.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserResponseMapper {

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .address(populateAddressDto(user.getAddress()))
                .build();
    }

    private AddressDTO populateAddressDto(Address address) {
        if(address != null){
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setId(address.getId());
            addressDTO.setStreet(address.getStreet());
            addressDTO.setCity(address.getCity());
            addressDTO.setState(address.getState());
            addressDTO.setZipcode(address.getZipCode());
            addressDTO.setCountry(address.getCountry());
            return addressDTO;
        }
        return new AddressDTO();
    }
}

