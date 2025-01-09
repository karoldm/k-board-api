package com.karoldm.k_board_api.dto;

import java.time.LocalDate;

public record UserDTO(
        String name,
        String email,
        LocalDate createdAt,
        String photoUrl
    ) {
}
