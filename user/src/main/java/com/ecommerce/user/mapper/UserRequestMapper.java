package com.ecommerce.user.mapper;

import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRequestMapper {

    // single source of truth for address mapping, shared with UserServiceImpl.updateUser
    private final AddressMapper addressMapper;

    public User mapToUserRequest(UserRequest request){
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .address(addressMapper.mapToAddress(request.getAddress()))
                .build();
    }
}
