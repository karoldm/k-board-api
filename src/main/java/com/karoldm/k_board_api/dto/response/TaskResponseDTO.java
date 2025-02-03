package com.karoldm.k_board_api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record TaskResponseDTO(
        UUID id,
        String title,
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        OffsetDateTime createdAt,
        String status,
        String color,
        Set<String> tags,
        UserResponseDTO createdBy,
        Set<UserResponseDTO> members
) {
}
