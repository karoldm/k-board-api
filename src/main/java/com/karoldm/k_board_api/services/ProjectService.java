package com.karoldm.k_board_api.services;

import com.karoldm.k_board_api.dto.payload.ProjectPayloadDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.repositories.ProjectRepository;
import com.karoldm.k_board_api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
        projectRepository.save(project);
        member.addProjectParticipated(project);
        userRepository.save(member);

        return project;
    }

    @Transactional
    public Project deleteMembersToProject(Project project, List<User> members) {

        project.removeMembers(new HashSet<>(members));
        projectRepository.save(project);

        for (User member : members) {
            member.removeProjectParticipated(project);
        }
        userRepository.saveAll(members);

        return project;
    }

    @Transactional
    public Project updateProject(String title, UUID id) {
        Project project = projectRepository.findById(id).orElse(null);
        if(project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + id);
        }

        project.setTitle(title);
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(UUID id) {
        projectRepository.deleteById(id);
    }
}
