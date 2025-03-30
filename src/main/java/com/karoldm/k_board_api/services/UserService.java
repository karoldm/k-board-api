package com.karoldm.k_board_api.services;

import com.karoldm.k_board_api.dto.payload.LoginPayloadDTO;
import com.karoldm.k_board_api.dto.payload.RegisterPayloadDTO;
import com.karoldm.k_board_api.dto.response.LoginResponseDTO;
import com.karoldm.k_board_api.dto.response.UserResponseDTO;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.exceptions.UserNotFoundException;
import com.karoldm.k_board_api.mappers.UserMapper;
import com.karoldm.k_board_api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository userRepository;
    private FileStorageService storageService;
    private final TokenService tokenService;
    private AuthenticationManager authenticationManager;

    public LoginResponseDTO authenticate(LoginPayloadDTO loginPayloadDTO){
        User user = userRepository.findByEmail(loginPayloadDTO.email());

        if(user == null) {
            throw new UserNotFoundException(String.format("User with email %s not founded", loginPayloadDTO.email()));
        }

        var usernamePassword = new UsernamePasswordAuthenticationToken(
                loginPayloadDTO.email(), loginPayloadDTO.password());

        var auth = authenticationManager.authenticate(usernamePassword);
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = tokenService.generateToken(userDetails.getUsername());

        UserResponseDTO userResponseDTO = UserMapper.toUserResponseDTO(user);

        return new LoginResponseDTO(token, userResponseDTO);
    }

    @Transactional
    public UserResponseDTO createUser(RegisterPayloadDTO registerPayloadDTO) {
        User existingUser = userRepository.findByEmail(registerPayloadDTO.email());

        if(existingUser != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered.");
        }

        String encryptedPassword = new BCryptPasswordEncoder().encode(registerPayloadDTO.password());
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);

        String photoKey = storageService.upload(registerPayloadDTO.photo());

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
}
