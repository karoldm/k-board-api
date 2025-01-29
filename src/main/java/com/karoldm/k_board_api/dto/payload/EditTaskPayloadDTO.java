package com.karoldm.k_board_api.dto.payload;

import com.karoldm.k_board_api.enums.TaskStatus;

public record EditTaskPayloadDTO(
        TaskStatus status
) {
}
