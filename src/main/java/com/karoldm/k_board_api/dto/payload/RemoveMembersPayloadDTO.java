package com.karoldm.k_board_api.dto.payload;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;
import java.util.UUID;

public record RemoveMembersPayloadDTO(
        @NotEmpty(message = "membersId cannot be empty")
        Set<UUID> membersId
) {
}
