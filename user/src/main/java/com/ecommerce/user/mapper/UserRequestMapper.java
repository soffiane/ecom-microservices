package com.ecommerce.user.mapper;

import com.ecommerce.user.dto.AddressDTO;
import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.model.Address;
import com.ecommerce.user.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserRequestMapper {

    public User mapToUserRequest(UserRequest request){
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .address(mapToAddress(request.getAddress()))
                .build();
    }

    private Address mapToAddress(AddressDTO addressDTO){
        if(addressDTO != null){
            return Address.builder()
                    .id(addressDTO.getId())
                    .street(addressDTO.getStreet())
                    .city(addressDTO.getCity())
                    .state(addressDTO.getState())
                    .zipCode(addressDTO.getZipcode())
                    .country(addressDTO.getCountry())
                    .build();
        }
        return new Address();
    }
}

