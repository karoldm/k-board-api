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
import com.karoldm.k_board_api.services.AuthService;
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
    private AuthService authService;

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
        lenient().when(authService.getSessionUser()).thenReturn(userMock);
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
}
