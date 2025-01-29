package com.karoldm.k_board_api.mappers;

import com.karoldm.k_board_api.dto.response.ProjectResponseDTO;
import com.karoldm.k_board_api.dto.response.UserResponseDTO;
import com.karoldm.k_board_api.entities.Project;

import java.util.Set;
import java.util.stream.Collectors;

public class ProjectMapper {

        public static ProjectResponseDTO toProjectResponseDTO(Project project) {

            Set<UserResponseDTO> userDTOs = project.getMembers().stream()
                    .map(UserMapper::toUserResponseDTO)
                    .collect(Collectors.toSet());

            return new ProjectResponseDTO(
                    project.getId(),
                    project.getTitle(),
                    project.getCreatedAt(),
                    userDTOs
            );
        }
}
