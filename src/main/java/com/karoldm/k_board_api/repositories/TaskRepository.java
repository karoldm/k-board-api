package com.karoldm.k_board_api.repositories;

import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByProject(Project project);

    @Query("""
        SELECT t FROM tasks t
        JOIN t.responsible r
        WHERE t.project = :project
        AND r.id = :responsibleId
    """)
    List<Task> findByProjectAndResponsibleContaining(
            @Param("project") Project project,
            @Param("responsibleId") UUID responsibleI

    );
}
