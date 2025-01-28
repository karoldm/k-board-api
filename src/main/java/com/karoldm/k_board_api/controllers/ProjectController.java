package com.karoldm.k_board_api.controllers;

import com.karoldm.k_board_api.dto.payload.AddMemberPayloadDTO;
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
import org.springframework.security.core.context.SecurityContextHolder;
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
        Project project = projectService.createProject(data, userService.getLoggedUser());

        return ResponseEntity.ok(ProjectMapper.toProjectResponseDTO(project));
    }

    @GetMapping
    private ResponseEntity<List<ProjectResponseDTO>> getAllProjectsByUser() {

        List<Project> projects = projectService.getAllProjectsByUserId(userService.getLoggedUser().getId());

        List<ProjectResponseDTO> responseProjects = projects.stream()
                .map(ProjectMapper::toProjectResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseProjects);
    }

    @PostMapping("/members")
    private ResponseEntity<?> addMember(@RequestBody AddMemberPayloadDTO addMemberDTO) {
        UUID projectId = addMemberDTO.projectId();
        Set<UUID> membersId = addMemberDTO.membersId();

        Optional<Project> project = projectService.findProjectById(projectId);
        if(project.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Project not found with ID: " + projectId);
        }

        User loggedUser = userService.getLoggedUser();

        if(loggedUser.getId() != project.get().getOwner().getId()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponseDTO(HttpStatus.FORBIDDEN.value(), "User aren't projects owner."));
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
}
