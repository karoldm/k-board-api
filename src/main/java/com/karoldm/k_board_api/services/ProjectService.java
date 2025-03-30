package com.karoldm.k_board_api.services;

import com.karoldm.k_board_api.dto.payload.AddMemberPayloadDTO;
import com.karoldm.k_board_api.dto.payload.EditProjectPayloadDTO;
import com.karoldm.k_board_api.dto.payload.ProjectPayloadDTO;
import com.karoldm.k_board_api.dto.response.ProjectResponseDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.mappers.ProjectMapper;
import com.karoldm.k_board_api.repositories.ProjectRepository;
import com.karoldm.k_board_api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    private AuthService authService;

    public Optional<Project> findProjectById(UUID id) {return projectRepository.findById(id);}

    @Transactional
    public ProjectResponseDTO createProject(ProjectPayloadDTO projectDTO) {
        User user = authService.getSessionUser();

        Project project = Project.builder()
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .title(projectDTO.title())
                .owner(user)
                .tasks(new HashSet<>())
                .members(new HashSet<>())
                .build();

        Project savedProject = projectRepository.save(project);

        return ProjectMapper.toProjectResponseDTO(savedProject);
    }

    @Transactional
    public ProjectResponseDTO addMemberToProject(AddMemberPayloadDTO data) {
        Project project = getProjectOrThrow(data.projectId());
        User member = authService.getSessionUser();

        if(member.getId() == project.getOwner().getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is project's owner.");
        }

        project.addMember(member);
        member.addProjectParticipated(project);
        userRepository.save(member);

        Project savedProject = projectRepository.save(project);

        return ProjectMapper.toProjectResponseDTO(savedProject);
    }

    @Transactional
    public Project deleteMembersToProject(Project project, List<User> members) {

        project.removeMembers(new HashSet<>(members));

        for (User member : members) {
            member.removeProjectParticipated(project);
        }
        userRepository.saveAll(members);

        return projectRepository.save(project);
    }

    @Transactional
    public ProjectResponseDTO updateProject(EditProjectPayloadDTO data, final UUID id) {
        checkProjectOwnership(id);

        Project project = projectRepository.findById(id).orElse(null);
        if(project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + id);
        }

        if(!data.membersIdToRemove().isEmpty()){
            List<User> users = userRepository.findAllById(data.membersIdToRemove());

            Set<UUID> missingUserIds = data.membersIdToRemove().stream()
                    .filter(memberId -> users.stream().noneMatch(user -> user.getId().equals(memberId)))
                    .collect(Collectors.toSet());

            if (!missingUserIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Users not found with IDs: " + missingUserIds);
            }

            this.deleteMembersToProject(project, users);
        }

        project.setTitle(data.title());

        Project savedProject = projectRepository.save(project);

        return ProjectMapper.toProjectResponseDTO(savedProject);
    }

    @Transactional
    public void deleteProject(UUID id) {
        checkProjectOwnership(id);
        Project project = getProjectOrThrow(id);

        for(User user: project.getMembers()){
            user.getProjects().remove(project);
        }

        projectRepository.deleteById(id);
    }

    public Page<ProjectResponseDTO> getAllProjectsByUser(
            String filter, int page, int size, String sortBy, String direction) {
        User user = authService.getSessionUser();

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Project> projectPage = projectRepository.findByOwnerAndTitleContainingIgnoreCase(
                user, filter, pageable);

        return projectPage.map(ProjectMapper::toProjectResponseDTO);
    }

    public Page<ProjectResponseDTO> getAllProjectsByUserParticipation(
            String filter, int page, int size, String sortBy, String direction) {

        User user = authService.getSessionUser();

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Project> projectPage = projectRepository
                .findByMembersContainsAndTitleContainingIgnoreCase(user, filter, pageable);

        return projectPage.map(ProjectMapper::toProjectResponseDTO);
    }

    public ProjectResponseDTO getProjectById(final UUID id) {
        checkProjectOwnershipOrParticipation(id);

        Project project = getProjectOrThrow(id);

        return ProjectMapper.toProjectResponseDTO(project);
    }

    private void checkProjectOwnershipOrParticipation(UUID projectId) {
        boolean isNotOwner = authService.getSessionUser().getProjects().stream().noneMatch(project -> project.getId().equals(projectId));
        boolean isNotMember = authService.getSessionUser().getParticipatedProjects().stream().noneMatch(project -> project.getId().equals(projectId));

        if (isNotMember && isNotOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this project");
        }
    }

    private void checkProjectOwnership(UUID projectId) {
        if (authService.getSessionUser().getProjects().stream().noneMatch(project -> project.getId().equals(projectId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this project");
        }
    }

    public Project getProjectOrThrow(UUID projectId) {
        return findProjectById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));
    }
}
