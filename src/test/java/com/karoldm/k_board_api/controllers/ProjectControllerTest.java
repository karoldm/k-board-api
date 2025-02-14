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
import com.karoldm.k_board_api.mappers.ProjectMapper;
import com.karoldm.k_board_api.services.ProjectService;
import com.karoldm.k_board_api.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
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
        lenient().when(userService.getSessionUser()).thenReturn(userMock);
    }

    @Test
    void shouldListProjectsThatUserIsOwner() throws Exception {
        UUID projectOwnerId = UUID.randomUUID();

        Project projectOwnerMock = createProjectMock(userMock, projectOwnerId);
        List<Project> projects = List.of(projectOwnerMock);

        List<ProjectResponseDTO> projectDTOs = projects.stream()
                .map(ProjectMapper::toProjectResponseDTO)
                .toList();

        Page<ProjectResponseDTO> projectPage = new PageImpl<>(projectDTOs, PageRequest.of(0, 10), projects.size());

        when(projectService.getAllProjectsByUser(any(User.class), anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(projectPage);

        MvcResult result = mockMvc.perform(get(OWNER_ENDPOINT)
                        .param("page", "0")
                        .param("size", "10")
                        .param("filter", "")
                        .param("sortBy", "createdAt")
                        .param("direction", "desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        verify(projectService)
                .getAllProjectsByUser(any(User.class), anyString(), anyInt(), anyInt(), anyString(), anyString());


        assertProjectResponse(result, projectOwnerId);
    }

    @Test
    void shouldListProjectsThatUserIsMember() throws Exception {
        UUID projectOwnerId = UUID.randomUUID();

        Project projectOwnerMock = createProjectMock(userMock, projectOwnerId);
        List<Project> projects = List.of(projectOwnerMock);

        List<ProjectResponseDTO> projectDTOs = projects.stream()
                .map(ProjectMapper::toProjectResponseDTO)
                .toList();

        Page<ProjectResponseDTO> projectPage = new PageImpl<>(projectDTOs, PageRequest.of(0, 10), projects.size());

        when(projectService.getAllProjectsByUserParticipation(
                any(User.class), anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(projectPage);

        MvcResult result = mockMvc.perform(get(MEMBER_ENDPOINT)
                        .param("page", "0")
                        .param("size", "10")
                        .param("filter", "")
                        .param("sortBy", "createdAt")
                        .param("direction", "desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        verify(projectService)
                .getAllProjectsByUserParticipation(
                        any(User.class), anyString(), anyInt(), anyInt(), anyString(), anyString());

        assertProjectResponse(result, projectOwnerId);
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
                .andExpect(jsonPath("$.members").isArray())
                .andExpect(jsonPath("$.progress").value(0.0));

        verify(projectService).createProject(payload, userMock);
    }

    @Test
    void shouldReturnBadRequestWhenAddingOwnerAsMember() throws Exception {
        UUID projectOwnerId = UUID.randomUUID();
        Project projectOwnerMock = createProjectMock(userMock, projectOwnerId);

        userMock.setProjects(new HashSet<>(List.of(projectOwnerMock)));

        AddMemberPayloadDTO payloadDTO = new AddMemberPayloadDTO(projectOwnerMock.getId());

        when(projectService.findProjectById(projectOwnerId)).thenReturn(Optional.of(projectOwnerMock));

        mockMvc.perform(post(MEMBER_ENDPOINT)
                        .content(objectMapper.writeValueAsString(payloadDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("User is project's owner."));
    }

    @Test
    void shouldReturnNotFoundWhenGetProjectByNonExistingID() throws Exception {
        String id = "123";

        mockMvc.perform(get(PROJECT_ENDPOINT + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found. Invalid UUID: " + id));
    }

    @Test
    void shouldReturnForbiddenWhenGetProjectByNonOwnerOrMemberID() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get(PROJECT_ENDPOINT + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You do not have permission to access this project"));

    }

    @Test
    void shouldReturnSuccessWhenGetProjectByID() throws Exception {
        UUID id = UUID.randomUUID();
        Project project = createProjectMock(userMock, id);
        userMock.setProjects(new HashSet<>(List.of(project)));

        when(projectService.findProjectById(id)).thenReturn(Optional.of(project));

        mockMvc.perform(get(PROJECT_ENDPOINT + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(projectService).findProjectById(id);
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

        Map<String, Object> responseMap = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

        List<ProjectResponseDTO> content = objectMapper.convertValue(responseMap.get("content"), new TypeReference<>() {});

        assertEquals(1, content.size());
        assertEquals(expectedProjectId, content.getFirst().id());
    }
}
