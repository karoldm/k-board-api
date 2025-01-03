package com.karoldm.k_board_api.entities;

import com.karoldm.k_board_api.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String title;
    private String description;
    private LocalDate createdAt;
    private TaskStatus status;
    private String color;
    //private ArrayList<String> tags = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
}
