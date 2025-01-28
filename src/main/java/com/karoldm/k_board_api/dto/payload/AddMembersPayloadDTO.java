package com.karoldm.k_board_api.dto.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record AddMembersPayloadDTO(
        @NotNull(message = "membersId cannot be null")
        @NotEmpty(message = "membersId cannot be empty")
        Set<UUID> membersId
) {
}
