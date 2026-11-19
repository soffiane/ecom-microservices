package com.ecommerce.user.dto;

import com.ecommerce.user.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;

    @NotBlank(message = "Phone is required")
    //@Pattern(message = "Phone is not valid", regexp = "^\\+?\\d{10,15}$")
    private String phone;

    //@NotNull(message = "Role is required")
    private UserRole role;

    private AddressDTO address;
}
