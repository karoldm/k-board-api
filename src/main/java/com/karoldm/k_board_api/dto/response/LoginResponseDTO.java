package com.karoldm.k_board_api.dto.response;

public record LoginResponseDTO(
        String token,
        UserResponseDTO user
) {
}
