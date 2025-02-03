package com.karoldm.k_board_api.mappers;

import com.karoldm.k_board_api.dto.response.TaskResponseDTO;
import com.karoldm.k_board_api.entities.Task;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.enums.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskMapperTest {
    @Test
    void shouldMapTaskToTaskResponseDTO() {

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

        UUID taskId = UUID.randomUUID();
        String title = "title";
        String description = "description";
        String status = TaskStatus.PENDING.toString();
        String color = "#000";

        HashSet<String> tags = new HashSet<>();
        tags.add("backend");

        HashSet<User> members = new HashSet<>();
        members.add(mockUser);

        Task mockTask = new Task();
        mockTask.setId(taskId);
        mockTask.setTitle(title);
        mockTask.setDescription(description);
        mockTask.setCreatedAt(createdAt);
        mockTask.setStatus(status);
        mockTask.setColor(color);
        mockTask.setCreatedBy(mockUser);
        mockTask.setTags(tags);
        mockTask.setResponsible(members);

        TaskResponseDTO taskResponseDTO = TaskMapper.toTaskResponseDTO(mockTask);

        assertEquals(taskId, taskResponseDTO.id());
        assertEquals(title, taskResponseDTO.title());
        assertEquals(description, taskResponseDTO.description());
        assertEquals(createdAt, taskResponseDTO.createdAt());
        assertEquals(color, taskResponseDTO.color());
        assertEquals(status, taskResponseDTO.status());
        assertEquals(UserMapper.toUserResponseDTO(mockUser), taskResponseDTO.createdBy());
        assertEquals(tags, taskResponseDTO.tags());
        assertEquals(
                members.stream().map(UserMapper::toUserResponseDTO).collect(Collectors.toSet()),
                taskResponseDTO.members()
        );
    }
}
