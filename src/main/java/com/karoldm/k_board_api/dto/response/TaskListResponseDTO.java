package com.karoldm.k_board_api.dto.response;

import java.util.List;

public record TaskListResponseDTO(
        List<TaskResponseDTO> pending,
        List<TaskResponseDTO> completed,
        List<TaskResponseDTO> doing,
        int total,
        int totalPending,
        int totalDoing,
        int totalCompleted
        ) {
}
