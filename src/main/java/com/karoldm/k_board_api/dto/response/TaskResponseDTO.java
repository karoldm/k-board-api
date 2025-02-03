package com.karoldm.k_board_api.dto.response;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record TaskResponseDTO(
        UUID id,
        String title,
        String description,
        OffsetDateTime createdAt,
        String status,
        String color,
        Set<String> tags,
        UserResponseDTO createdBy,
        Set<UserResponseDTO> members
) {
}
