package com.karoldm.k_board_api.services;

import com.karoldm.k_board_api.dto.payload.RegisterPayloadDTO;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository userRepository;
    private FileStorageService storageService;

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User createUser(RegisterPayloadDTO data) {
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);

        String photoKey = storageService.upload(data.photo());

        User user = new User();
        user.setEmail(data.email());
        user.setPassword(encryptedPassword);
        user.setCreatedAt(createdAt);
        user.setName(data.name());
        user.setPhotoUrl(photoKey);

        return userRepository.save(user);
    }

    public List<User> findAllUsersById(Set<UUID> ids) {
        return userRepository.findAllById(ids);
    }

    public User getSessionUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return this.findUserByEmail(email);
    }
}
