package com.karoldm.k_board_api.services;

import com.karoldm.k_board_api.dto.payload.AddMemberPayloadDTO;
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
}
