package com.karoldm.k_board_api.repositories;

import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    Page<Task> findByProject(Project project, Pageable pageable);

    @Query("""
        SELECT t FROM tasks t
        JOIN t.responsible r
        WHERE t.project = :project
        AND r.id = :responsibleId
    """)
    Page<Task> findByProjectAndResponsibleContaining(
            @Param("project") Project project,
            @Param("responsibleId") UUID responsibleId,
            Pageable pageable
    );
}
