package com.tenderpilot.identity.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(
                message = "Email is required"
        )
        @Email(
                message = "Email address is invalid"
        )
        String email,


        @NotBlank(
                message = "Password is required"
        )
        @Size(
                max = 100,
                message = "Password is too long"
        )
        String password

) {
}
