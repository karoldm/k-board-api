package com.karoldm.k_board_api.dto.payload;

import jakarta.validation.constraints.NotEmpty;

public record EditPasswordPayloadDTO(
        @NotEmpty
        String password
) {
}
