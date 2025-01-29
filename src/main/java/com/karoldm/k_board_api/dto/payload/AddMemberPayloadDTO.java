package com.karoldm.k_board_api.dto.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddMemberPayloadDTO(
        @NotEmpty(message = "memberId cannot be empty")
        UUID memberId
) {
}
