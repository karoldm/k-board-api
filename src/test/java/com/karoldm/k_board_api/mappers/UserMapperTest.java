package com.karoldm.k_board_api.mappers;

import com.karoldm.k_board_api.dto.response.UserResponseDTO;
import com.karoldm.k_board_api.entities.User;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void shouldMapUserToUserResponseDTO() {

        UUID userId = UUID.randomUUID();
        String name = "John";
        String email = "john.doe@example.com";
        String photoUrl = "photo_url";
        OffsetDateTime createdAt = OffsetDateTime.now();

        User user = new User();
        user.setId(userId);
        user.setName(name);
        user.setEmail(email);
        user.setPhotoUrl(photoUrl);
        user.setCreatedAt(createdAt);

        UserResponseDTO userResponseDTO = UserMapper.toUserResponseDTO(user);

        assertEquals(userId, userResponseDTO.id());
        assertEquals(name, userResponseDTO.name());
        assertEquals(email, userResponseDTO.email());
        assertEquals(photoUrl, userResponseDTO.photoUrl());
        assertEquals(createdAt, userResponseDTO.createdAt());
    }
}
