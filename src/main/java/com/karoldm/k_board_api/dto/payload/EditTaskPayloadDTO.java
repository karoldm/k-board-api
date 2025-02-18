package com.karoldm.k_board_api.dto.payload;

import com.karoldm.k_board_api.enums.TaskStatus;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record EditTaskPayloadDTO(
        Optional<TaskStatus> status,
        Optional<String> title,
        Optional<String> description,
        Optional<String> color,
        Optional<Set<String>> tags,
        Optional<Set<UUID>> responsible
) {
}
