package com.karoldm.k_board_api.controllers;

import com.karoldm.k_board_api.dto.payload.AddMembersPayloadDTO;
import com.karoldm.k_board_api.dto.payload.ProjectPayloadDTO;
import com.karoldm.k_board_api.dto.response.ErrorResponseDTO;
import com.karoldm.k_board_api.dto.response.ProjectResponseDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.mappers.ProjectMapper;
import com.karoldm.k_board_api.services.ProjectService;
import com.karoldm.k_board_api.services.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/project")
@AllArgsConstructor
public class ProjectController {

    private ProjectService projectService;
    private UserService userService;

    @PostMapping
    private ResponseEntity<ProjectResponseDTO> createProject(@RequestBody ProjectPayloadDTO data) {
        Project project = projectService.createProject(data, userService.getSessionUser());

        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(project));
    }

    @GetMapping
    private ResponseEntity<List<ProjectResponseDTO>> getAllProjectsByUser() {

        List<Project> projects = projectService.getAllProjectsByUserId(userService.getSessionUser().getId());

        List<ProjectResponseDTO> responseProjects = projects.stream()
                .map(ProjectMapper::toProjectResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseProjects);
    }

    @PutMapping("/{id}/members")
    @PreAuthorize("@ownershipSecurity.isOwner(#id)")
    private ResponseEntity<?> addMembers(@RequestBody AddMembersPayloadDTO addMemberDTO, @PathVariable UUID id) {
        Set<UUID> membersId = addMemberDTO.membersId();

        Optional<Project> project = projectService.findProjectById(id);
        if(project.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Project not found with ID: " + id);
        }

        List<User> users = userService.findAllUsersById(membersId);

        Set<UUID> missingUserIds = membersId.stream()
                .filter(memberId -> users.stream().noneMatch(user -> user.getId().equals(memberId)))
                .collect(Collectors.toSet());

        if (!missingUserIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Users not found with IDs: " + missingUserIds);
        }

        Project updatedProject = projectService.addMemberToProject(project.get(), users);

        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(updatedProject));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ownershipSecurity.isOwner(#id)")
    private ResponseEntity<ProjectResponseDTO> updateProject(@RequestBody ProjectPayloadDTO data, @PathVariable UUID id) {
        Project updatedProject = projectService.updateProject(data.title(), id);
        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(updatedProject));
    }

}
