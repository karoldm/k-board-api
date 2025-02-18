package com.karoldm.k_board_api.mappers;

import com.karoldm.k_board_api.dto.response.TaskResponseDTO;
import com.karoldm.k_board_api.dto.response.UserResponseDTO;
import com.karoldm.k_board_api.entities.Task;

import java.util.Set;
import java.util.stream.Collectors;

public class TaskMapper {
    private TaskMapper(){}

    public static TaskResponseDTO toTaskResponseDTO(Task task) {
        Set<UserResponseDTO> responsible = task.getResponsible().stream()
                .map(UserMapper::toUserResponseDTO)
                .collect(Collectors.toSet());

        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCreatedAt(),
                task.getStatus(),
                task.getColor(),
                task.getTags(),
                UserMapper.toUserResponseDTO(task.getCreatedBy()),
                responsible
        );
    }
}
