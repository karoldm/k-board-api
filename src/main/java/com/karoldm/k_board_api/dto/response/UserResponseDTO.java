package com.karoldm.k_board_api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String email,
        String photoUrl,
        OffsetDateTime createdAt
    ) {
}
