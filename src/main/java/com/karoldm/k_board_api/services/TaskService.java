package com.karoldm.k_board_api.services;

import com.karoldm.k_board_api.dto.payload.TaskPayloadDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.Task;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.enums.TaskStatus;
import com.karoldm.k_board_api.repositories.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public Optional<Task> findTaskById(UUID id) {return taskRepository.findById(id);}

    @Transactional
    public Task createTask(TaskPayloadDTO data, User createdBy, Project project, Set<User> members){

        Task task = new Task();

        task.setTags(data.tags());
        task.setDescription(data.description());
        task.setTitle(data.title());
        task.setColor(data.color());

        task.setCreatedAt(LocalDate.now());
        task.setStatus(TaskStatus.PENDING.toString());

        task.setProject(project);
        task.setCreatedBy(createdBy);
        task.setResponsible(members);

        return taskRepository.save(task);
    }

    @Transactional
    public Task addMembersToTask(Task task, Set<User> members){
        task.setResponsible(members);
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }

}
