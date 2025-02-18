package com.karoldm.k_board_api.mappers;

import com.karoldm.k_board_api.dto.response.ProjectResponseDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.User;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectMapperTest {
    @Test
    void shouldMapProjectToProjectResponseDTO() {
        UUID userId = UUID.randomUUID();
        String name = "John";
        String email = "john.doe@example.com";
        String photoUrl = "photo_url";
        OffsetDateTime createdAt = OffsetDateTime.now();

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName(name);
        mockUser.setEmail(email);
        mockUser.setPhotoUrl(photoUrl);
        mockUser.setCreatedAt(createdAt);

        UUID projectId = UUID.randomUUID();
        String titleProject = "title project";

        HashSet<User> membersProject = new HashSet<>();
        membersProject.add(mockUser);

        Project mockProject = new Project();
        mockProject.setId(projectId);
        mockProject.setTitle(titleProject);
        mockProject.setCreatedAt(createdAt);
        mockProject.setMembers(membersProject);
        mockProject.setOwner(mockUser);

        ProjectResponseDTO projectResponseDTO = ProjectMapper.toProjectResponseDTO(mockProject);

        assertEquals(projectId, projectResponseDTO.id());
        assertEquals(titleProject, projectResponseDTO.title());
        assertEquals(createdAt, projectResponseDTO.createdAt());
        assertEquals(mockUser.getId(), projectResponseDTO.owner().id());
        assertEquals(mockUser.getName(), projectResponseDTO.owner().name());
        assertEquals(mockUser.getEmail(), projectResponseDTO.owner().email());
        assertEquals(mockUser.getPhotoUrl(), projectResponseDTO.owner().photoUrl());
        assertEquals(mockUser.getCreatedAt(), projectResponseDTO.owner().createdAt());
        assertEquals(
                membersProject.stream().map(UserMapper::toUserResponseDTO).collect(Collectors.toSet()),
                projectResponseDTO.members()
        );
        assertEquals(0.0, projectResponseDTO.progress());
    }
}
