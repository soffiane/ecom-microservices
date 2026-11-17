package com.ecommerce.user.service;

import com.ecommerce.user.model.User;
import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();

    UserResponse addUser(UserRequest user);

    Optional<User> findById(String id);

    Optional<User> updateUser(String id, UserRequest user);
}
