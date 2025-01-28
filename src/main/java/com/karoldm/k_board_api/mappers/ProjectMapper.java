package com.karoldm.k_board_api.mappers;

import com.karoldm.k_board_api.dto.response.ProjectResponseDTO;
import com.karoldm.k_board_api.dto.response.TaskResponseDTO;
import com.karoldm.k_board_api.dto.response.UserResponseDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.Task;
import com.karoldm.k_board_api.entities.User;

import java.util.Set;
import java.util.stream.Collectors;

public class ProjectMapper {

        public static ProjectResponseDTO toProjectResponseDTO(Project project) {
            Set<TaskResponseDTO> taskDTOs = project.getTasks().stream()
                    .map(ProjectMapper::toTaskResponseDTO)
                    .collect(Collectors.toSet());

            Set<UserResponseDTO> userDTOs = project.getMembers().stream()
                    .map(ProjectMapper::toUserResponseDTO)
                    .collect(Collectors.toSet());

            return new ProjectResponseDTO(
                    project.getId(),
                    project.getTitle(),
                    project.getCreatedAt(),
                    taskDTOs,
                    userDTOs
            );
        }

        public static TaskResponseDTO toTaskResponseDTO(Task task) {
            return new TaskResponseDTO(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getCreatedAt(),
                    task.getStatus(),
                    task.getColor()
            );
        }

        public static UserResponseDTO toUserResponseDTO(User user) {
            return new UserResponseDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhotoUrl(),
                    user.getCreatedAt()
            );
        }

}
