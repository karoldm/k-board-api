package com.karoldm.k_board_api.dto.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record EditProjectPayloadDTO(
        @NotNull(message = "membersIdToRemove cannot be null")
        Set<UUID> membersIdToRemove,
        @NotEmpty(message = "Title cannot be empty")
        String title
) {
}
