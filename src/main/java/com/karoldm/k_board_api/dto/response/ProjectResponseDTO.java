package com.karoldm.k_board_api.dto.response;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record ProjectResponseDTO(
        UUID id,
        String title,
        OffsetDateTime createdAt,
        UserResponseDTO owner,
        Set<UserResponseDTO> members
) {
}
