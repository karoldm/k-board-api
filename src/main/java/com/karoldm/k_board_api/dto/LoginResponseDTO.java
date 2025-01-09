package com.karoldm.k_board_api.dto;

public record LoginResponseDTO(
        String token,
        UserDTO user
) {
}
