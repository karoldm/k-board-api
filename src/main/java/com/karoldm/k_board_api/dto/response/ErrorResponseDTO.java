package com.karoldm.k_board_api.dto.response;

public record ErrorResponseDTO(
        int status,
        String message
) {
}
