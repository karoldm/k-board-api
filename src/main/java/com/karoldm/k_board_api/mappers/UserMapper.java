package com.karoldm.k_board_api.mappers;

import com.karoldm.k_board_api.dto.response.UserResponseDTO;
import com.karoldm.k_board_api.entities.User;

public class UserMapper {
    private UserMapper(){}

    public static UserResponseDTO toUserResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhotoUrl(),
                user.getCreatedAt()
        );
    }
}
