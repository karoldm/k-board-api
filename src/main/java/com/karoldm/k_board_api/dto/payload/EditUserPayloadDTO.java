package com.karoldm.k_board_api.dto.payload;

import org.springframework.web.multipart.MultipartFile;

public record EditUserPayloadDTO(
        String name,
        MultipartFile photo
) {
}
