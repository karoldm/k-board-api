package com.karoldm.k_board_api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    private String email;
    private String photoUrl;
    private String password;
    private LocalDate createdAt;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private Set<Project> projects = new HashSet<>();
}
