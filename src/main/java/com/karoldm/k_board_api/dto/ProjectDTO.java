package com.karoldm.k_board_api.dto;

import java.util.Set;
import java.util.UUID;

public record ProjectDTO(
        String title,
        UUID ownerId,
        Set<UUID> tasksId
) {
}
