package com.karoldm.k_board_api.controllers;

import com.karoldm.k_board_api.dto.payload.EditTaskPayloadDTO;
import com.karoldm.k_board_api.dto.payload.TaskPayloadDTO;
import com.karoldm.k_board_api.dto.response.ErrorResponseDTO;
import com.karoldm.k_board_api.dto.response.TaskListResponseDTO;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/task")
@RequiredArgsConstructor
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
        TaskResponseDTO task = taskService.createTask(data);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/{projectId}")
    @Operation(
            summary = "get tasks by project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "get successfully"),
            @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "user is not owner neither member of the project", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
    public ResponseEntity<TaskListResponseDTO> getAllTasksByProject(
            @PathVariable UUID projectId, @RequestParam Optional<UUID> memberId
          ) {
        TaskListResponseDTO responseTasks = taskService.getTasksByProject(projectId, memberId);
        return ResponseEntity.ok(responseTasks);
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
    public ResponseEntity<TaskResponseDTO> editTask(
            @PathVariable UUID taskId,
            @RequestBody @Valid EditTaskPayloadDTO data
    ) {
        TaskResponseDTO updatedTask = taskService.editTask(data, taskId);
        return ResponseEntity.ok(updatedTask);

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
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
