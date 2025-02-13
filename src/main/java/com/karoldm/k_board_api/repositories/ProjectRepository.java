package com.karoldm.k_board_api.repositories;

import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Page<Project> findByOwnerAndTitleContainingIgnoreCase(User owner, String filter, Pageable pageable);

    Page<Project> findByMembersContainsAndTitleContainingIgnoreCase(User member, String filter, Pageable pageable);
}
