package com.karoldm.k_board_api.services;

import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.repositories.ProjectRepository;
import com.karoldm.k_board_api.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private User member1;
    private User member2;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(UUID.randomUUID());
        project.setTitle("Project test");

        member1 = new User();
        member1.setId(UUID.randomUUID());
        member1.setName("Member 1");

        member2 = new User();
        member2.setId(UUID.randomUUID());
        member2.setName("Member 2");
    }

    @Test
    void shouldAddMemberToProject() {
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(userRepository.save(any(User.class))).thenReturn(member1);

        Project updatedProject = projectService.addMemberToProject(project, member1);

        assertNotNull(updatedProject);
        assertTrue(updatedProject.getMembers().contains(member1));
        assertTrue(member1.getParticipatedProjects().contains(updatedProject));

        verify(projectRepository, times(1)).save(project);
        verify(userRepository, times(1)).save(member1);
    }

    @Test
    void shouldDeleteMembersFromProject() {
        List<User> members = Arrays.asList(member1, member2);
        project.setMembers(new HashSet<>(members));
        member1.setParticipatedProjects(new HashSet<>(List.of(project)));
        member2.setParticipatedProjects(new HashSet<>(List.of(project)));

        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(userRepository.saveAll(anyList())).thenReturn(members);

        Project updatedProject = projectService.deleteMembersToProject(project, members);

        assertNotNull(updatedProject);
        assertFalse(updatedProject.getMembers().contains(member1));
        assertFalse(updatedProject.getMembers().contains(member2));

        verify(projectRepository, times(1)).save(project);
        verify(userRepository, times(1)).saveAll(members);
    }

    @Test
    void shouldUpdateProjectTitle() {
        String newTitle = "Updated Project Title";
        when(projectRepository.findById(any(UUID.class))).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project updatedProject = projectService.updateProject(newTitle, new HashSet<>(), project.getId());

        assertNotNull(updatedProject);
        assertEquals(newTitle, updatedProject.getTitle());
        verify(projectRepository, times(1)).findById(project.getId());
        verify(projectRepository, times(1)).save(updatedProject);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingProject() {
        String newTitle = "Updated Project Title";
        when(projectRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            projectService.updateProject(newTitle, new HashSet(), project.getId());
        });
        assertEquals("Project not found with id: " + project.getId(), exception.getReason());
    }
}
