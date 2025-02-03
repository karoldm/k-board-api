package com.karoldm.k_board_api.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.karoldm.k_board_api.dto.payload.AddMemberPayloadDTO;
import com.karoldm.k_board_api.dto.payload.ProjectPayloadDTO;
import com.karoldm.k_board_api.dto.response.ProjectResponseDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.handlers.GlobalExceptionHandler;
import com.karoldm.k_board_api.services.ProjectService;
import com.karoldm.k_board_api.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    private static final String PROJECT_ENDPOINT = "/project";
    private static final String OWNER_ENDPOINT = PROJECT_ENDPOINT + "/owner";
    private static final String MEMBER_ENDPOINT = PROJECT_ENDPOINT + "/member";

    private MockMvc mockMvc;

    @InjectMocks
    private ProjectController projectController;

    @Mock
    private ProjectService projectService;
    @Mock
    private UserService userService;

    private User userMock;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        mockMvc = MockMvcBuilders.standaloneSetup(projectController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        userMock = createUserMock();
        when(userService.getSessionUser()).thenReturn(userMock);
    }

    @Test
    void shouldListProjectsThatUserIsOwner() throws Exception {
        UUID projectOwnerId = UUID.randomUUID();
        UUID projectMemberId = UUID.randomUUID();

        Project projectOwnerMock = createProjectMock(userMock, projectOwnerId);
        Project projectMemberMock = createProjectMock(userMock, projectMemberId);

        userMock.setProjects(new HashSet<>(List.of(projectOwnerMock)));
        userMock.setParticipatedProjects(new HashSet<>(List.of(projectMemberMock)));

        MvcResult result = mockMvc.perform(get(OWNER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        assertProjectResponse(result, projectOwnerId);
    }

    @Test
    void shouldListProjectsThatUserIsMember() throws Exception {
        UUID projectOwnerId = UUID.randomUUID();
        UUID projectMemberId = UUID.randomUUID();

        Project projectOwnerMock = createProjectMock(userMock, projectOwnerId);
        Project projectMemberMock = createProjectMock(userMock, projectMemberId);

        userMock.setProjects(new HashSet<>(List.of(projectOwnerMock)));
        userMock.setParticipatedProjects(new HashSet<>(List.of(projectMemberMock)));

        MvcResult result = mockMvc.perform(get(MEMBER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        assertProjectResponse(result, projectMemberId);
    }

    @Test
    void shouldCreateProjectSuccessfully() throws Exception {
        String title = "project title";
        UUID projectId = UUID.randomUUID();

        ProjectPayloadDTO payload = new ProjectPayloadDTO(title);
        Project projectMock = createProjectMock(userMock, projectId, title);

        when(projectService.createProject(payload, userMock)).thenReturn(projectMock);

        mockMvc.perform(post(PROJECT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(projectMock.getTitle()))
                .andExpect(jsonPath("$.createdAt").value(projectMock.getCreatedAt().toString()))
                .andExpect(jsonPath("$.owner.id").value(userMock.getId().toString()))
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.members").isArray());

        verify(projectService).createProject(payload, userMock);
    }

    @Test
    void shouldReturnBadRequestWhenAddingOwnerAsMember() throws Exception {
        UUID projectOwnerId = UUID.randomUUID();
        Project projectOwnerMock = createProjectMock(userMock, projectOwnerId);

        userMock.setProjects(new HashSet<>(List.of(projectOwnerMock)));

        AddMemberPayloadDTO payloadDTO = new AddMemberPayloadDTO(projectOwnerMock.getId());

        when(projectService.findProjectById(projectOwnerId)).thenReturn(Optional.of(projectOwnerMock));

        mockMvc.perform(put(MEMBER_ENDPOINT)
                        .content(objectMapper.writeValueAsString(payloadDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("User is project's owner."));
    }

    private User createUserMock() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setPassword("1234");
        user.setName("John");
        user.setCreatedAt(OffsetDateTime.now());
        user.setPhotoUrl("photo_url");
        return user;
    }

    private Project createProjectMock(User owner, UUID projectId) {
        return createProjectMock(owner, projectId, "default title");
    }

    private Project createProjectMock(User owner, UUID projectId, String title) {
        Project project = new Project();
        project.setId(projectId);
        project.setTitle(title);
        project.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS));
        project.setOwner(owner);
        return project;
    }

    private void assertProjectResponse(MvcResult result, UUID expectedProjectId) throws Exception {
        assertEquals(MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentType());
        List<ProjectResponseDTO> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(1, response.size());
        assertEquals(expectedProjectId, response.getFirst().id());
    }
}
