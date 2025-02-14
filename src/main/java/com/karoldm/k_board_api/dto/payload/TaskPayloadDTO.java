package com.karoldm.k_board_api.dto.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record TaskPayloadDTO(
    @NotNull(message = "projectId cannot be null")
    UUID projectId,

    @NotEmpty(message = "title cannot be empty")
    String title,

    @NotEmpty(message = "description cannot be empty")
    String description,

    @NotEmpty(message = "color cannot be empty")
    String color,

    @NotNull(message = "tags cannot be null")
    Set<String> tags,

    @NotNull(message = "membersId cannot be null")
    Set<UUID> membersId
) {
}
