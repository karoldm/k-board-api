package com.karoldm.k_board_api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.karoldm.k_board_api.dto.payload.LoginPayloadDTO;
import com.karoldm.k_board_api.dto.payload.RegisterPayloadDTO;
import com.karoldm.k_board_api.dto.response.LoginResponseDTO;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.handlers.GlobalExceptionHandler;
import com.karoldm.k_board_api.services.TokenService;
import com.karoldm.k_board_api.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private User createMockUser(String email, String password, String name, UUID id) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setId(id);
        user.setPhotoUrl("");
        user.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS));
        return user;
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        String email = "user@example.com";
        String password = "1234";
        String token = "generated-jwt-token";
        UUID userId = UUID.randomUUID();
        User userMock = createMockUser(email, password, "John", userId);

        Authentication authenticationMock = mock(Authentication.class);
        UserDetails userDetailsMock = mock(UserDetails.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authenticationMock);
        when(authenticationMock.getPrincipal()).thenReturn(userDetailsMock);
        when(userDetailsMock.getUsername()).thenReturn(email);

        when(userService.findUserByEmail(email)).thenReturn(userMock);
        when(tokenService.generateToken(email)).thenReturn(token);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayloadDTO(email, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.user.id").value(userId.toString()))
                .andExpect(jsonPath("$.user.name").value("John"))
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.photoUrl").value(""))
                .andReturn();

        LoginResponseDTO loginResponse = objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponseDTO.class);
        assertEquals(userMock.getCreatedAt(), loginResponse.user().createdAt());

        verify(userService).findUserByEmail(email);
        verify(tokenService).generateToken(email);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldReturnBadRequestForInvalidLogin() throws Exception {
        testBadRequestOnLogin("", "1234", "Email cannot be empty\r\n");
        testBadRequestOnLogin("invalid_email", "1234", "Invalid email format\r\n");
        testBadRequestOnLogin("user@example.com", "", "Password cannot be empty\r\n");
    }

    private void testBadRequestOnLogin(String email, String password, String expectedMessage) throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayloadDTO(email, password))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @Test
    public void shouldReturnBadRequestIfEmailExist() throws Exception {
        String email = "user@example.com";
        String password = "1234";
        String name = "john";
        UUID userId = UUID.randomUUID();
        User userMock = createMockUser(email, password, name, userId);

        when(userService.findUserByEmail(email)).thenReturn(userMock);

        mockMvc.perform(multipart("/auth/register")
                        .param("email", email)
                        .param("name", name)
                        .param("password", password)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email already registered."));
    }

    @Test
    public void shuldRegisterUserSuccessfully() throws Exception {
        String email = "user@example.com";
        String password = "1234";
        String name = "john";
        UUID userId = UUID.randomUUID();
        User userMock = createMockUser(email, password, name, userId);

        RegisterPayloadDTO payloadDTO = new RegisterPayloadDTO(name, email, password, null);

        when(userService.findUserByEmail(email)).thenReturn(null);
        when(userService.createUser(payloadDTO)).thenReturn(userMock);

        mockMvc.perform(multipart("/auth/register")
                        .param("email", payloadDTO.email())
                        .param("name", payloadDTO.name())
                        .param("password", payloadDTO.password())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userMock.getId().toString()))
                .andExpect(jsonPath("$.name").value(userMock.getName()))
                .andExpect(jsonPath("$.email").value(userMock.getEmail()))
                .andExpect(jsonPath("$.photoUrl").value(""))
                .andExpect(jsonPath("$.createdAt").value(userMock.getCreatedAt().truncatedTo(ChronoUnit.MILLIS).toString()));
    }

}