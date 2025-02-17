package com.karoldm.k_board_api.services;

import com.karoldm.k_board_api.dto.payload.ProjectPayloadDTO;
import com.karoldm.k_board_api.dto.response.ProjectResponseDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.mappers.ProjectMapper;
import com.karoldm.k_board_api.repositories.ProjectRepository;
import com.karoldm.k_board_api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class ProjectService {
    private ProjectRepository projectRepository;
    private UserRepository userRepository;

    public Optional<Project> findProjectById(UUID id) {return projectRepository.findById(id);}

    @Transactional
    public Project createProject(ProjectPayloadDTO projectDTO, User loggedUser) {
        Project project = new Project();

        project.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        project.setTitle(projectDTO.title());
        project.setOwner(loggedUser);
        project.setTasks(new HashSet<>());

        return projectRepository.save(project);
    }

    @Transactional
    public Project addMemberToProject(Project project, User member) {
        project.addMember(member);
        member.addProjectParticipated(project);
        userRepository.save(member);

        return projectRepository.save(project);
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
    public Project updateProject(String title, Set<UUID> membersToRemove, UUID id) {
        Project project = projectRepository.findById(id).orElse(null);
        if(project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + id);
        }

        if(!membersToRemove.isEmpty()){
            List<User> users = userRepository.findAllById(membersToRemove);

            Set<UUID> missingUserIds = membersToRemove.stream()
                    .filter(memberId -> users.stream().noneMatch(user -> user.getId().equals(memberId)))
                    .collect(Collectors.toSet());

            if (!missingUserIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Users not found with IDs: " + missingUserIds);
            }

            this.deleteMembersToProject(project, users);
        }

        project.setTitle(title);
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(UUID id) {
        projectRepository.deleteById(id);
    }

    @Transactional
    public Page<ProjectResponseDTO> getAllProjectsByUser(
            User user, String filter, int page, int size, String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Project> projectPage = projectRepository.findByOwnerAndTitleContainingIgnoreCase(
                user, filter, pageable);

        return projectPage.map(ProjectMapper::toProjectResponseDTO);
    }

    public Page<ProjectResponseDTO> getAllProjectsByUserParticipation(
            User user, String filter, int page, int size, String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Project> projectPage = projectRepository
                .findByMembersContainsAndTitleContainingIgnoreCase(user, filter, pageable);

        return projectPage.map(ProjectMapper::toProjectResponseDTO);
    }
}
