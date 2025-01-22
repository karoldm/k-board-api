package com.karoldm.k_board_api.controllers;

import com.karoldm.k_board_api.dto.LoginDTO;
import com.karoldm.k_board_api.dto.LoginResponseDTO;
import com.karoldm.k_board_api.dto.RegisterDTO;
import com.karoldm.k_board_api.dto.UserDTO;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.services.FileStorageService;
import com.karoldm.k_board_api.services.TokenService;
import com.karoldm.k_board_api.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final TokenService tokenService;
    private AuthenticationManager authenticationManager;
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = authenticationManager.authenticate(usernamePassword);

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = tokenService.generateToken(userDetails.getUsername());

        User user = userService.findUserByEmail(userDetails.getUsername());

        return ResponseEntity.ok().body(new LoginResponseDTO(token, new UserDTO(
                user.getName(),
                user.getEmail(),
                user.getPhotoUrl(),
                user.getCreatedAt()
        )));
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerUser(@ModelAttribute @Valid RegisterDTO data) {

        if(userService.findUserByEmail(data.email()) != null){
            return ResponseEntity.badRequest().body("Email already registered.");
        }

        User user = userService.createUser(data);
        return ResponseEntity.ok().body(new UserDTO(
                user.getName(),
                user.getEmail(),
                user.getPhotoUrl(),
                user.getCreatedAt()
        ));
    }
}
