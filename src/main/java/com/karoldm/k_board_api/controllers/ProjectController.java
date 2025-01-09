package com.karoldm.k_board_api.controllers;

import com.karoldm.k_board_api.dto.ProjectDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.services.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project")
@AllArgsConstructor
public class ProjectController {

    private ProjectService projectService;

    @PostMapping
    private ResponseEntity<Project> createProject(@RequestBody ProjectDTO data) {
        Project project = projectService.createProject(data);

        return ResponseEntity.ok(project);
    }

    @GetMapping
    private ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }
}
