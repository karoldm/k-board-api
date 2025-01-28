package com.karoldm.k_board_api.dto.response;

import com.karoldm.k_board_api.enums.TaskStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record TaskResponseDTO(
        UUID id,
        String title,
        String description,
        LocalDate createdAt,
        TaskStatus status,
        String color
) {
}
