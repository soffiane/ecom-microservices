package com.ecommerce.user.service;

import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.mapper.AddressMapper;
import com.ecommerce.user.mapper.UserRequestMapper;
import com.ecommerce.user.mapper.UserResponseMapper;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;
    private final UserRequestMapper userRequestMapper;
    private final AddressMapper addressMapper;

    @Override
    public List<User> findAll() {
        return userRepository.findAll()
                .stream()
                .toList();
    }

    @Override
    public UserResponse addUser(UserRequest userRequest) {
        User user = userRequestMapper.mapToUserRequest(userRequest);
        return userResponseMapper.mapToUserResponse(userRepository.save(user));
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> updateUser(String id, UserRequest user) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setFirstName(user.getFirstName());
                    existingUser.setLastName(user.getLastName());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setPhone(user.getPhone());
                    existingUser.setRole(user.getRole());
                    existingUser.setAddress(addressMapper.mapToAddress(user.getAddress()));
                    return userRepository.save(existingUser);
                });
    }
}
