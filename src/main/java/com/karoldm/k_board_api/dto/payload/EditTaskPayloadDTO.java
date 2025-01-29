package com.karoldm.k_board_api.dto.payload;

import com.karoldm.k_board_api.enums.TaskStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record EditTaskPayloadDTO(
        TaskStatus status,

        @NotEmpty(message = "Title cannot be empty")
        String title,

        @NotEmpty(message = "Description cannot be empty")
        String description,

        @NotEmpty(message = "color cannot be empty")
        String color,

        @NotNull(message = "tags cannot be null")
        Set<String> tags
) {
}
