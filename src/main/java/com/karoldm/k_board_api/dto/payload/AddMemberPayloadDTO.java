package com.karoldm.k_board_api.dto.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record AddMemberPayloadDTO(
        @NotNull(message = "membersId cannot be null")
        @NotEmpty(message = "membersId cannot be empty")
        Set<UUID> membersId,
        @NotNull(message = "projectId cannot be null")
        @NotEmpty(message = "projectId cannot be empty")
        UUID projectId
) {
}
