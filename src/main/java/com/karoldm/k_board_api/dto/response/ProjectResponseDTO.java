package com.karoldm.k_board_api.dto.response;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ProjectResponseDTO(
        UUID id,
        String title,
        LocalDate createdAt,
        UserResponseDTO owner,
        Set<UserResponseDTO> members
) {
}
