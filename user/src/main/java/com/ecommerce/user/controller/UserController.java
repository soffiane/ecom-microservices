package com.ecommerce.user.controller;

import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.mapper.UserResponseMapper;
import com.ecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User account management")
public class UserController {

    private final UserService userService;
    private final UserResponseMapper userResponseMapper;

    @GetMapping
    @Operation(summary = "List users", description = "Returns every user. No pagination, filtering or sorting.")
    @ApiResponse(responseCode = "200", description = "List of users (possibly empty)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body((userService.findAll())
                .stream()
                .map(userResponseMapper::mapToUserResponse)
                .toList());
    }

    @PostMapping
    @Operation(summary = "Create a user",
            description = "Email must be unique. The `role` is taken from the request as-is.")
    @ApiResponse(responseCode = "201", description = "User created")
    @ApiResponse(responseCode = "400", description = "Validation failed (body uses the default Spring Boot error format)",
            content = @Content)
    @ApiResponse(responseCode = "409", description = "Email already used by another user",
            content = @Content(mediaType = "text/plain",
                    schema = @Schema(type = "string", example = "Email already exists")))
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addUser(user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by id")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "No user with this id", content = @Content)
    public ResponseEntity<UserResponse> getUserById(@Parameter(description = "User id (MongoDB ObjectId string)") @PathVariable String id) {
        return userService.findById(id)
                .map(userResponseMapper::mapToUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a user",
            description = "Overwrites all fields, including `role` and `address` (an omitted address is removed).")
    @ApiResponse(responseCode = "200", description = "User updated")
    @ApiResponse(responseCode = "400", description = "Validation failed (body uses the default Spring Boot error format)",
            content = @Content)
    @ApiResponse(responseCode = "404", description = "No user with this id", content = @Content)
    @ApiResponse(responseCode = "409", description = "Email already used by another user",
            content = @Content(mediaType = "text/plain",
                    schema = @Schema(type = "string", example = "Email already exists")))
    public ResponseEntity<UserResponse> updateUser(@Parameter(description = "User id (MongoDB ObjectId string)") @PathVariable String id,
                                                   @RequestBody @Valid UserRequest user) {
        return userService.updateUser(id, user)
                .map(userResponseMapper::mapToUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
