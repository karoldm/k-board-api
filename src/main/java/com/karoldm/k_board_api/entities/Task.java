package com.karoldm.k_board_api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotEmpty(message = "title cannot be empty")
    private String title;

    @NotEmpty(message = "description cannot be empty")
    private String description;

    private OffsetDateTime createdAt;

    @NotEmpty(message = "status cannot be empty")
    private String status;

    @NotEmpty(message = "color cannot be empty")
    private String color;

    @ElementCollection
    private Set<String> tags = new HashSet<>();

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonBackReference
    private Project project;

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name="task_atribuization",
            joinColumns = @JoinColumn(name="task_id"),
            inverseJoinColumns = @JoinColumn(name="user_id")
    )
    @JsonBackReference
    private Set<User> responsible = new HashSet<>();
}
