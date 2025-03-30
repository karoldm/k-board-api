package com.karoldm.k_board_api.controllers;

import com.karoldm.k_board_api.dto.payload.AddMemberPayloadDTO;
import com.karoldm.k_board_api.dto.payload.ProjectPayloadDTO;
import com.karoldm.k_board_api.dto.payload.EditProjectPayloadDTO;
import com.karoldm.k_board_api.dto.response.ErrorResponseDTO;
import com.karoldm.k_board_api.dto.response.ProjectResponseDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.mappers.ProjectMapper;
import com.karoldm.k_board_api.services.ProjectService;
import com.karoldm.k_board_api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import lombok.AllArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/project")
@AllArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    @PostMapping()
    @Operation(
            summary = "Create new project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created successfully"),
            @ApiResponse(responseCode = "400", description = "invalid body data", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
    public ResponseEntity<ProjectResponseDTO> createProject(@RequestBody @Valid ProjectPayloadDTO data) {
        ProjectResponseDTO project = projectService.createProject(data);
        return ResponseEntity.status (HttpStatus.CREATED).body(project);
    }

    @GetMapping("/owner")
    @Operation(
            summary = "List all projects that user is owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "get successfully"),
            @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
    public ResponseEntity<Page<ProjectResponseDTO>>  getAllProjectsByUser(
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Page<ProjectResponseDTO> responseProjects = projectService
                .getAllProjectsByUser(filter, page, size, sortBy, direction);

        return ResponseEntity.ok(responseProjects);
    }

    @GetMapping("/member")
    @Operation(
            summary = "List all projects that user is member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "get successfully"),
            @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
    public ResponseEntity<Page<ProjectResponseDTO>> getAllProjectsByUserParticipation(
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Page<ProjectResponseDTO> responseProjects = projectService
                .getAllProjectsByUserParticipation(filter, page, size, sortBy, direction);

        return ResponseEntity.ok(responseProjects);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a project by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "deleted successfully"),
            @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "the user is not project's owner", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/member")
    @Operation(
            summary = "Add user to a project as member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "added successfully"),
            @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "project not found with the id", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "invalid body data", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
    public ResponseEntity<ProjectResponseDTO> addMember(@RequestBody @Valid AddMemberPayloadDTO data) {
        ProjectResponseDTO updatedProject = projectService.addMemberToProject(data);
        return ResponseEntity.ok(updatedProject);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "edit project's data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "edited successfully"),
            @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "user is not project's owner", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "invalid body data", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
    public ResponseEntity<ProjectResponseDTO> updateProject(@RequestBody @Valid EditProjectPayloadDTO data, @PathVariable UUID id) {
        ProjectResponseDTO updatedProject = projectService.updateProject(data, id);
        return ResponseEntity.ok(updatedProject);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable UUID id) {
        ProjectResponseDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

}
