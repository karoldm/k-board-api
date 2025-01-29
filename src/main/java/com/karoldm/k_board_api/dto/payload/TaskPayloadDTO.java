package com.karoldm.k_board_api.dto.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;


import java.util.Set;
import java.util.UUID;

public record TaskPayloadDTO(
    @NotEmpty(message = "title cannot be empty")
    String title,

    @NotEmpty(message = "description cannot be empty")
    String description,

    @NotEmpty(message = "color cannot be empty")
    String color,

    @NotEmpty(message = "tags cannot be empty")
    Set<String> tags,

    Set<UUID> membersId
) {
}
