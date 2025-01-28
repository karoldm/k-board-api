package com.karoldm.k_board_api.services;

import com.karoldm.k_board_api.dto.payload.ProjectPayloadDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.repositories.ProjectRepository;
import com.karoldm.k_board_api.repositories.UserRepository;
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

    @Transactional
    public Project createProject(ProjectPayloadDTO projectDTO, User loggedUser) {
        Project project = new Project();

        project.setCreatedAt(LocalDate.now());
        project.setTitle(projectDTO.title());
        project.setOwner(loggedUser);
        project.setTasks(new HashSet<>());

        return projectRepository.save(project);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<Project> findProjectById(UUID id) {return projectRepository.findById(id);}

    @Transactional
    public Project addMemberToProject(Project project, List<User> members) {

        project.addMembers(new HashSet<>(members));
        projectRepository.save(project);

        for (User member : members) {
            member.addProjectParticipated(project);
        }
        userRepository.saveAll(members);

        return project;
    }
}
