package com.karoldm.k_board_api.controllers;

import com.karoldm.k_board_api.dto.payload.AddResponsiblePayloadDTO;
import com.karoldm.k_board_api.dto.payload.EditTaskPayloadDTO;
import com.karoldm.k_board_api.dto.payload.TaskPayloadDTO;
import com.karoldm.k_board_api.dto.response.ErrorResponseDTO;
import com.karoldm.k_board_api.dto.response.TaskResponseDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.Task;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.mappers.TaskMapper;
import com.karoldm.k_board_api.services.ProjectService;
import com.karoldm.k_board_api.services.TaskService;
import com.karoldm.k_board_api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "create a new task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created successfully"),
            @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "invalid body data", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "user is not owner neither member of the project", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),

    })
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
    @Operation(
            summary = "get tasks by project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "get successfully"),
            @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "user is not owner neither member of the project", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
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
    @Operation(
            summary = "add a member to a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "added successfully"),
            @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "user is not owner neither member of the project", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "task not found", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
    public ResponseEntity<TaskResponseDTO> addMember(@PathVariable UUID taskId, @RequestBody @Valid AddResponsiblePayloadDTO data) {

        Optional<Task> task = taskService.findTaskById(taskId);

        if(task.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + taskId);
        }

        checkProjectOwnershipOrParticipation(task.get().getProject().getId());

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

    @PutMapping("/{taskId}")
    @Operation(
            summary = "edit task's info")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "edited successfully"),
            @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "user is not owner neither member of the project", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "task not found", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
    public ResponseEntity<TaskResponseDTO> editTask(@PathVariable UUID taskId, @RequestBody @Valid EditTaskPayloadDTO data) {

        Optional<Task> task = taskService.findTaskById(taskId);

        if(task.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + taskId);
        }

        checkProjectOwnershipOrParticipation(task.get().getProject().getId());

        Task updatedTask = taskService.editTask(task.get(), data);

        return ResponseEntity.ok(TaskMapper.toTaskResponseDTO(updatedTask));
    }

    @DeleteMapping("/{taskId}")
    @Operation(
            summary = "delete task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "deleted successfully"),
            @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "user is not owner neither member of the project", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "task not found", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId) {
        Optional<Task> task = taskService.findTaskById(taskId);

        if(task.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + taskId);
        }

        checkProjectOwnershipOrParticipation(task.get().getProject().getId());

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
