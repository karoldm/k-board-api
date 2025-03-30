package com.karoldm.k_board_api.services;


import com.karoldm.k_board_api.dto.payload.LoginPayloadDTO;
import com.karoldm.k_board_api.dto.payload.RegisterPayloadDTO;
import com.karoldm.k_board_api.dto.response.LoginResponseDTO;
import com.karoldm.k_board_api.dto.response.UserResponseDTO;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.exceptions.UserNotAuthenticated;
import com.karoldm.k_board_api.exceptions.UserNotFoundException;
import com.karoldm.k_board_api.mappers.UserMapper;
import com.karoldm.k_board_api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final FileStorageService storageService;

    private AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserDetails user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getAuthorities());
    }

    public LoginResponseDTO authenticateUser(LoginPayloadDTO loginPayloadDTO) throws Exception {
        User user = userRepository.findByEmail(loginPayloadDTO.email());
        if (user == null) {
            throw new UserNotFoundException(String.format("User with email %s not found", loginPayloadDTO.email()));
        }

        var usernamePassword = new UsernamePasswordAuthenticationToken(
                loginPayloadDTO.email(), loginPayloadDTO.password());

        var auth = authenticationManager().authenticate(usernamePassword);
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = tokenService.generateToken(userDetails.getUsername());

        return new LoginResponseDTO(token, UserMapper.toUserResponseDTO(user));
    }

    @Transactional
    public UserResponseDTO registerUser(RegisterPayloadDTO registerPayloadDTO) {
        User existingUser = userRepository.findByEmail(registerPayloadDTO.email());

        if(existingUser != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered.");
        }

        String encryptedPassword = new BCryptPasswordEncoder().encode(registerPayloadDTO.password());
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);

        String photoKey = storageService.uploadFile(registerPayloadDTO.photo());

        User newUser = User.builder()
                .email(registerPayloadDTO.email())
                .password(encryptedPassword)
                .createdAt(createdAt)
                .name(registerPayloadDTO.name())
                .photoUrl(photoKey)
                .build();

        User saveduser = userRepository.save(newUser);

        return UserMapper.toUserResponseDTO(saveduser);
    }

    public User getSessionUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email);

        if(user == null){
            throw new UserNotAuthenticated("User is not logged.");
        }

        return user;
    }
}
