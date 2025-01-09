package com.karoldm.k_board_api.services;

import com.karoldm.k_board_api.dto.ProjectDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.repositories.ProjectRepository;
import com.karoldm.k_board_api.repositories.TaskRepository;
import com.karoldm.k_board_api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Service
@AllArgsConstructor
public class ProjectService {
    private ProjectRepository projectRepository;
    private UserRepository userRepossitory;
    private TaskRepository taskRepository;

    @Transactional
    public Project createProject(ProjectDTO projectDTO) {
        Project project = new Project();

        project.setCreatedAt(LocalDate.now());
        project.setTitle(projectDTO.title());
        project.setOwner(userRepossitory.findById(projectDTO.ownerId()).orElse(null));
        project.setTasks(new HashSet<>(taskRepository.findAllById(projectDTO.tasksId())));

        return projectRepository.save(project);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
}
