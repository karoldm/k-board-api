package com.karoldm.k_board_api.dto.payload;


import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddMemberPayloadDTO(
        @NotNull(message = "projectId cannot be null")
        UUID projectId
) {
}
