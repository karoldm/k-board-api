package com.karoldm.k_board_api.dto.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record LoginPayloadDTO(
        @NotNull(message = "Email cannot be null")
        @NotEmpty(message = "Email cannot be empty")
        @Email(message = "Invalid email format")
        String email,
        @NotNull(message = "Password cannot be null")
        @NotEmpty(message = "Password cannot be empty")
        String password
) {
}
