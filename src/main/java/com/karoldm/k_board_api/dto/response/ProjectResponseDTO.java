package com.karoldm.k_board_api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record ProjectResponseDTO(
        UUID id,
        String title,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        OffsetDateTime createdAt,
        UserResponseDTO owner,
        Set<UserResponseDTO> members
) {
}
