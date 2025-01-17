package com.karoldm.k_board_api.services;

import com.karoldm.k_board_api.dto.RegisterDTO;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository userRepository;
    private FileStorageService storageService;

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User createUser(RegisterDTO data) {
        String encryptedPasswrod = new BCryptPasswordEncoder().encode(data.password());
        LocalDate createdAt = LocalDate.now();

        String photoKey = storageService.upload(data.photo());

        User user = new User();
        user.setEmail(data.email());
        user.setPassword(encryptedPasswrod);
        user.setCreatedAt(createdAt);
        user.setName(data.name());
        user.setPhotoUrl(photoKey);

        return userRepository.save(user);
    }

}
