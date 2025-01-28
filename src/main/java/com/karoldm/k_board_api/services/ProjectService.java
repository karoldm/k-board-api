package com.karoldm.k_board_api.services;

import com.amazonaws.services.codestar.model.ProjectNotFoundException;
import com.amazonaws.services.kms.model.NotFoundException;
import com.karoldm.k_board_api.dto.payload.ProjectPayloadDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.repositories.ProjectRepository;
import com.karoldm.k_board_api.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
public class ProjectService {
    private ProjectRepository projectRepository;
    private UserRepository userRepository;

    public List<Project> getAllProjectsByUserId(UUID userId) {
        return projectRepository.findAll().stream().filter(project -> project.getOwner().getId() == userId).toList();
    }

    public Optional<Project> findProjectById(UUID id) {return projectRepository.findById(id);}

    @Transactional
    public Project createProject(ProjectPayloadDTO projectDTO, User loggedUser) {
        Project project = new Project();

        project.setCreatedAt(LocalDate.now());
        project.setTitle(projectDTO.title());
        project.setOwner(loggedUser);
        project.setTasks(new HashSet<>());

        return projectRepository.save(project);
    }

    @Transactional
    public Project addMembersToProject(Project project, List<User> members) {

        project.addMembers(new HashSet<>(members));
        projectRepository.save(project);

        for (User member : members) {
            member.addProjectParticipated(project);
        }
        userRepository.saveAll(members);

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
            throw new NotFoundException("Project not found with id: " + id);
        }

        project.setTitle(title);
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(UUID id) {
        projectRepository.deleteById(id);
    }
}
