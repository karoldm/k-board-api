package com.karoldm.k_board_api.dto;

public record ErrorResponseDTO(
        int status,
        String message
) {
}
