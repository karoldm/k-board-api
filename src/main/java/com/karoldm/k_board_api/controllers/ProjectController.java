package com.karoldm.k_board_api.controllers;

import com.amazonaws.services.kms.model.NotFoundException;
import com.amazonaws.services.pinpoint.model.ForbiddenException;
import com.karoldm.k_board_api.dto.payload.AddMemberPayloadDTO;
import com.karoldm.k_board_api.dto.payload.ProjectPayloadDTO;
import com.karoldm.k_board_api.dto.payload.RemoveMembersPayloadDTO;
import com.karoldm.k_board_api.dto.response.ProjectResponseDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.mappers.ProjectMapper;
import com.karoldm.k_board_api.services.ProjectService;
import com.karoldm.k_board_api.services.UserService;
import org.springframework.http.ResponseEntity;
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

    private final ProjectService projectService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(@RequestBody ProjectPayloadDTO data) {
        Project project = projectService.createProject(data, userService.getSessionUser());
        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(project));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjectsByUser() {
        Set<Project> projects = userService.getSessionUser().getProjects();
        List<ProjectResponseDTO> responseProjects = projects.stream()
                .map(ProjectMapper::toProjectResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseProjects);
    }

    @GetMapping("/member")
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjectsByUserParticipation() {
        Set<Project> projects = userService.getSessionUser().getParticipatedProjects();
        List<ProjectResponseDTO> responseProjects = projects.stream()
                .map(ProjectMapper::toProjectResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseProjects);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        checkProjectOwnership(id);
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/member")
    public ResponseEntity<ProjectResponseDTO> addMembers(@RequestBody AddMemberPayloadDTO addMemberDTO, @PathVariable UUID id) {
        Project project = getProjectOrThrow(id);
        Optional<User> member = userService.findUserById(addMemberDTO.memberId());

        if (member.isEmpty()) {
            throw new NotFoundException("User not found with ID: " + addMemberDTO.memberId());
        }

        Project updatedProject = projectService.addMemberToProject(project, member.get());
        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(updatedProject));
    }

    @DeleteMapping("/{id}/members")
    public ResponseEntity<ProjectResponseDTO> deleteMembers(@RequestBody RemoveMembersPayloadDTO removeMembersPayloadDTO, @PathVariable UUID id) {
        Project project = getProjectOrThrow(id);
        checkProjectOwnership(id);

        Set<UUID> membersId = removeMembersPayloadDTO.membersId();
        List<User> users = userService.findAllUsersById(membersId);

        Set<UUID> missingUserIds = membersId.stream()
                .filter(memberId -> users.stream().noneMatch(user -> user.getId().equals(memberId)))
                .collect(Collectors.toSet());

        if (!missingUserIds.isEmpty()) {
            throw new NotFoundException("Users not found with IDs: " + missingUserIds);
        }

        Project updatedProject = projectService.deleteMembersToProject(project, users);
        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(updatedProject));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> updateProject(@RequestBody ProjectPayloadDTO data, @PathVariable UUID id) {
        checkProjectOwnership(id);
        Project updatedProject = projectService.updateProject(data.title(), id);
        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(updatedProject));
    }

    private void checkProjectOwnership(UUID projectId) {
        if (userService.getSessionUser().getProjects().stream().noneMatch(project -> project.getId().equals(projectId))) {
            throw new ForbiddenException("You do not have permission to access this project");
        }
    }

    private Project getProjectOrThrow(UUID projectId) {
        return projectService.findProjectById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found with id: " + projectId));
    }
}
