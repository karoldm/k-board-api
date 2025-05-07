package com.karoldm.k_board_api.services;

import com.karoldm.k_board_api.dto.payload.EditPasswordPayloadDTO;
import com.karoldm.k_board_api.dto.payload.EditUserPayloadDTO;
import com.karoldm.k_board_api.dto.response.UserResponseDTO;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.exceptions.InvalidPasswordException;
import com.karoldm.k_board_api.mappers.UserMapper;
import com.karoldm.k_board_api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FileStorageService storageService;
    private final AuthService authService;

    private final int MIN_PASSWORD_SIZE = 8;

    @Transactional
    public void updatePassword(EditPasswordPayloadDTO editPasswordPayloadDTO) {
        User user = authService.getSessionUser();

        if (editPasswordPayloadDTO.password().length() < MIN_PASSWORD_SIZE) {
            throw new InvalidPasswordException("The password must have at least 8 character.");
        }

        String encryptedPassword = new BCryptPasswordEncoder().encode(editPasswordPayloadDTO.password());
        user.setPassword(encryptedPassword);

        userRepository.save(user);
    }

    @Transactional
    public UserResponseDTO updateUser(EditUserPayloadDTO editUserPayloadDTO) {
        User user = authService.getSessionUser();

        if (editUserPayloadDTO.name() != null) {
            user.setName(editUserPayloadDTO.name());
        }

        if (editUserPayloadDTO.photo() != null) {
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                storageService.removeFileByUrl(user.getPhotoUrl());
            }
            String photoKey = storageService.uploadFile(editUserPayloadDTO.photo());
            user.setPhotoUrl(photoKey);
        }

        User saveduser = userRepository.save(user);

        return UserMapper.toUserResponseDTO(saveduser);
    }

}
