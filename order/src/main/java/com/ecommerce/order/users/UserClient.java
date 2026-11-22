package com.ecommerce.order.users;

import com.ecommerce.order.dto.UserResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Optional;

@HttpExchange("/api/v1/users")
public interface UserClient {

    @GetExchange("/{id}")
    Optional<UserResponse> getUserById(@PathVariable String id);
}
