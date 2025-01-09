package com.karoldm.k_board_api.repositories;

import com.karoldm.k_board_api.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
}
