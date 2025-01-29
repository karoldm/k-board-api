package com.karoldm.k_board_api.dto.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ProjectPayloadDTO(
        @NotEmpty(message = "Title cannot be empty")
        String title
) {
}
