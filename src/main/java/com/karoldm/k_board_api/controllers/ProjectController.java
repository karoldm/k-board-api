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

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        User userLogged = userService.getSessionUser();
        Project project = projectService.createProject(data, userLogged);
        return ResponseEntity.status (HttpStatus.CREATED).body(ProjectMapper.toProjectResponseDTO(project));
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
        User user = userService.getSessionUser();

        Page<ProjectResponseDTO> responseProjects = projectService
                .getAllProjectsByUser(user, filter, page, size, sortBy, direction);

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
        User user = userService.getSessionUser();

        Page<ProjectResponseDTO> responseProjects = projectService
                .getAllProjectsByUserParticipation(user, filter, page, size, sortBy, direction);

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
        checkProjectOwnership(id);
        Project project = getProjectOrThrow(id);

        for(User user: project.getMembers()){
            user.getProjects().remove(project);
        }

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
        Project project = getProjectOrThrow(data.projectId());
        User member = userService.getSessionUser();

        if(member.getId() == project.getOwner().getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is project's owner.");
        }

        Project updatedProject = projectService.addMemberToProject(project, member);
        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(updatedProject));
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
        checkProjectOwnership(id);
        Project updatedProject = projectService.updateProject(data.title(), data.membersIdToRemove(), id);
        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(updatedProject));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable UUID id) {
        checkProjectOwnershipOrParticipation(id);

        Project project = getProjectOrThrow(id);

        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(project));
    }

    private void checkProjectOwnershipOrParticipation(UUID projectId) {
        boolean isNotOwner = userService.getSessionUser().getProjects().stream().noneMatch(project -> project.getId().equals(projectId));
        boolean isNotMember = userService.getSessionUser().getParticipatedProjects().stream().noneMatch(project -> project.getId().equals(projectId));
        if (isNotMember && isNotOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this project");
        }
    }

    private void checkProjectOwnership(UUID projectId) {
        if (userService.getSessionUser().getProjects().stream().noneMatch(project -> project.getId().equals(projectId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this project");
        }
    }

    public Project getProjectOrThrow(UUID projectId) {
        return projectService.findProjectById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));
    }
}
