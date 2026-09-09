package com.tenderpilot.identity.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(
                message = "Full name is required"
        )
        @Size(
                max = 160,
                message = "Full name must not exceed 160 characters"
        )
        String fullName,


        @NotBlank(
                message = "Organization name is required"
        )
        @Size(
                max = 180,
                message = "Organization name must not exceed 180 characters"
        )
        String organizationName,


        @NotBlank(
                message = "Email is required"
        )
        @Email(
                message = "Email address is invalid"
        )
        @Size(
                max = 320,
                message = "Email must not exceed 320 characters"
        )
        String email,


        @NotBlank(
                message = "Password is required"
        )
        @Size(
                min = 10,
                max = 100,
                message = "Password must contain between 10 and 100 characters"
        )
        String password

) {
}
