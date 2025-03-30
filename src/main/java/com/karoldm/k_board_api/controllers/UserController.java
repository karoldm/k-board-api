package com.karoldm.k_board_api.controllers;

import com.karoldm.k_board_api.dto.payload.EditUserPayloadDTO;
import com.karoldm.k_board_api.dto.response.ErrorResponseDTO;
import com.karoldm.k_board_api.dto.response.UserResponseDTO;
import com.karoldm.k_board_api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Edit user",
            description = "Edit user's information as name and photo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "created successfully"),
            @ApiResponse(responseCode = "400", description = "invalid body data", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
    public ResponseEntity<UserResponseDTO> updateUser(@ModelAttribute @Valid EditUserPayloadDTO editUserRequestDTO) {
        UserResponseDTO userResponseDTO = userService.updateUser(editUserRequestDTO);
        return ResponseEntity.ok(userResponseDTO);
    }
}
