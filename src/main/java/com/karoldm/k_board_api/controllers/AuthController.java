package com.karoldm.k_board_api.controllers;

import com.karoldm.k_board_api.dto.LoginDTO;
import com.karoldm.k_board_api.dto.RegisterDTO;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.repositories.UserRepository;
import com.karoldm.k_board_api.services.TokenService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final TokenService tokenService;
    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Validated LoginDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = authenticationManager.authenticate(usernamePassword);

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = tokenService.generateToken(userDetails.getUsername());

        return ResponseEntity.ok().body(token);
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerUser(@ModelAttribute @Validated RegisterDTO data) {

        if(userRepository.findByEmail(data.email()) != null){
            return ResponseEntity.badRequest().build();
        }

        String encryptedPasswrod = new BCryptPasswordEncoder().encode(data.password());
        LocalDate createdAt = LocalDate.now();

        User user = new User();
        user.setEmail(data.email());
        user.setPassword(encryptedPasswrod);
        user.setCreatedAt(createdAt);
        user.setName(data.name());

        userRepository.save(user);

        return ResponseEntity.ok().body(user);
    }
}
