package com.karoldm.k_board_api.controllers;

import com.karoldm.k_board_api.dto.payload.AddMemberPayloadDTO;
import com.karoldm.k_board_api.dto.payload.AddResponsiblePayloadDTO;
import com.karoldm.k_board_api.dto.payload.TaskPayloadDTO;
import com.karoldm.k_board_api.dto.response.ProjectResponseDTO;
import com.karoldm.k_board_api.dto.response.TaskResponseDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.Task;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.mappers.ProjectMapper;
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

import java.util.*;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/task")
@AllArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final UserService userService;
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody @Valid TaskPayloadDTO data) {
        checkProjectOwnershipOrParticipation(data.projectId());
        Project project = getProjectOrThrow(data.projectId());

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

    @GetMapping("/{projectId}")
    public ResponseEntity<Set<TaskResponseDTO>> getAllTasksByProject(@PathVariable UUID projectId, @RequestParam Optional<UUID> memberId) {
        checkProjectOwnershipOrParticipation(projectId);
        Project project = getProjectOrThrow(projectId);

        Set<TaskResponseDTO> responseTasks = project.getTasks().stream()
                .map(TaskMapper::toTaskResponseDTO)
                .collect(Collectors.toSet());

        if(memberId.isPresent()){
            responseTasks = responseTasks.stream()
                    .filter(task -> task.members()
                            .stream()
                            .anyMatch(responsible -> responsible.id() == memberId.get()))
                    .collect(Collectors.toSet());
        }


        return ResponseEntity.ok(responseTasks);
    }

    @PutMapping("/{taskId}/member")
    public ResponseEntity<TaskResponseDTO> addMember(@PathVariable UUID taskId, @RequestBody @Valid AddResponsiblePayloadDTO data) {

        Optional<Task> task = taskService.findTaskById(taskId);

        if(task.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + taskId);
        }

        Set<UUID> membersId = data.membersId();
        Project project = task.get().getProject();

        Set<User> projectMembers = new HashSet<>(project.getMembers());

        Set<UUID> missingUserIds = membersId.stream()
                .filter(memberId -> projectMembers.stream().noneMatch(user -> user.getId().equals(memberId)))
                .collect(Collectors.toSet());

        if (!missingUserIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Users not found with IDs: " + missingUserIds);
        }

        Task updatedTask = taskService.addMembersToTask(task.get(), projectMembers);
        return ResponseEntity.ok(TaskMapper.toTaskResponseDTO(updatedTask));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId) {
        Optional<Task> task = taskService.findTaskById(taskId);

        if(task.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + taskId);
        }

        taskService.deleteTask(taskId);

        return ResponseEntity.noContent().build();
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
