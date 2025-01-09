package com.karoldm.k_board_api.repositories;

import com.karoldm.k_board_api.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
}
