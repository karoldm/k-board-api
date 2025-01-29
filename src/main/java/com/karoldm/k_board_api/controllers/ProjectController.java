package com.karoldm.k_board_api.controllers;

import com.karoldm.k_board_api.dto.payload.AddMemberPayloadDTO;
import com.karoldm.k_board_api.dto.payload.ProjectPayloadDTO;
import com.karoldm.k_board_api.dto.payload.RemoveMembersPayloadDTO;
import com.karoldm.k_board_api.dto.response.ProjectResponseDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.mappers.ProjectMapper;
import com.karoldm.k_board_api.services.ProjectService;
import com.karoldm.k_board_api.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
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

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(@RequestBody @Valid ProjectPayloadDTO data) {
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
    public ResponseEntity<ProjectResponseDTO> addMember(@RequestBody @Valid AddMemberPayloadDTO addMemberDTO, @PathVariable UUID id) {
        Project project = getProjectOrThrow(id);
        Optional<User> member = userService.findUserById(addMemberDTO.memberId());

        if (member.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + addMemberDTO.memberId());
        }

        if(member.get().getId() == userService.getSessionUser().getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is project's owner.");
        }

        Project updatedProject = projectService.addMemberToProject(project, member.get());
        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(updatedProject));
    }

    @DeleteMapping("/{id}/members")
    public ResponseEntity<ProjectResponseDTO> deleteMembers(@RequestBody @Valid RemoveMembersPayloadDTO removeMembersPayloadDTO, @PathVariable UUID id) {
        Project project = getProjectOrThrow(id);
        checkProjectOwnership(id);

        Set<UUID> membersId = removeMembersPayloadDTO.membersId();
        List<User> users = userService.findAllUsersById(membersId);

        Set<UUID> missingUserIds = membersId.stream()
                .filter(memberId -> users.stream().noneMatch(user -> user.getId().equals(memberId)))
                .collect(Collectors.toSet());

        if (!missingUserIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Users not found with IDs: " + missingUserIds);
        }

        Project updatedProject = projectService.deleteMembersToProject(project, users);
        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(updatedProject));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> updateProject(@RequestBody @Valid ProjectPayloadDTO data, @PathVariable UUID id) {
        checkProjectOwnership(id);
        Project updatedProject = projectService.updateProject(data.title(), id);
        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(updatedProject));
    }

    private void checkProjectOwnership(UUID projectId) {
        if (userService.getSessionUser().getProjects().stream().noneMatch(project -> project.getId().equals(projectId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this project");
        }
    }

    private Project getProjectOrThrow(UUID projectId) {
        return projectService.findProjectById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));
    }
}
