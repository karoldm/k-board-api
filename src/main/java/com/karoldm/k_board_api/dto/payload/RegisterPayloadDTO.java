package com.karoldm.k_board_api.dto.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

public record RegisterPayloadDTO(
        @NotEmpty(message = "Name cannot be empty")
        String name,
        @NotEmpty(message = "Email cannot be empty")
        @Email(message = "Invalid email format")
        String email,
        @NotEmpty(message = "Password cannot be empty")
        String password,
        MultipartFile photo
) {
}
