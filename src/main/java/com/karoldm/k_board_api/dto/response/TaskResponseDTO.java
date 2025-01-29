package com.karoldm.k_board_api.dto.response;

import com.karoldm.k_board_api.enums.TaskStatus;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record TaskResponseDTO(
        UUID id,
        String title,
        String description,
        LocalDate createdAt,
        String status,
        String color,
        Set<String> tags,
        UserResponseDTO createdBy,
        Set<UserResponseDTO> members
) {
}
