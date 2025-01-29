package com.karoldm.k_board_api.controllers;

import com.karoldm.k_board_api.dto.payload.TaskPayloadDTO;
import com.karoldm.k_board_api.dto.response.TaskResponseDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.Task;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.mappers.TaskMapper;
import com.karoldm.k_board_api.services.ProjectService;
import com.karoldm.k_board_api.services.TaskService;
import com.karoldm.k_board_api.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/project/{projectId}/task")
@AllArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final UserService userService;
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@PathVariable UUID projectId, @RequestBody @Valid TaskPayloadDTO data) {
        checkProjectOwnershipOrParticipation(projectId);
        Project project = getProjectOrThrow(projectId);

        Set<UUID> membersId = data.membersId();
        Set<User> members = new HashSet<>(userService.findAllUsersById(membersId));

        Set<UUID> missingUserIds = membersId.stream()
                .filter(memberId -> members.stream().noneMatch(user -> user.getId().equals(memberId)))
                .collect(Collectors.toSet());

        if (!missingUserIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Users not found with IDs: " + missingUserIds);
        }

        Task task = taskService.createTask(data, userService.getSessionUser(), project, members);

        return ResponseEntity.ok(TaskMapper.toTaskResponseDTO(task));
    }

    private void checkProjectOwnershipOrParticipation(UUID projectId) {
        boolean isOwner = userService.getSessionUser().getProjects().stream().anyMatch(project -> project.getId().equals(projectId));
        boolean isMember = userService.getSessionUser().getParticipatedProjects().stream().anyMatch(project -> project.getId().equals(projectId));
        if (!isOwner && !isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this project");
        }
    }


    private Project getProjectOrThrow(UUID projectId) {
        return projectService.findProjectById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));
    }
}
