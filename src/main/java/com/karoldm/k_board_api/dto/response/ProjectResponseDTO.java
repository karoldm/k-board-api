package com.karoldm.k_board_api.dto.response;

import com.karoldm.k_board_api.entities.Task;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ProjectResponseDTO(
        UUID id,
        String title,
        LocalDate createdAt,
        Set<TaskResponseDTO> tasks,
        Set<UserResponseDTO> members
) {
}
