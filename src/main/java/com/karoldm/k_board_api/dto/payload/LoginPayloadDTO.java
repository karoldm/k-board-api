package com.karoldm.k_board_api.dto.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record LoginPayloadDTO(
        @NotEmpty(message = "Email cannot be empty")
        @Email(message = "Invalid email format")
        String email,
        @NotEmpty(message = "Password cannot be empty")
        String password
) {
}
