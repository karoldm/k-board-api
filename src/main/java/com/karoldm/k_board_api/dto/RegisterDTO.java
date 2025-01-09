package com.karoldm.k_board_api.dto;

import org.springframework.web.multipart.MultipartFile;

public record RegisterDTO(
        String name,
        String email,
        String password,
        MultipartFile photo
) {
}
