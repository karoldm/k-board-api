package com.karoldm.k_board_api.services;

import com.karoldm.k_board_api.dto.payload.EditTaskPayloadDTO;
import com.karoldm.k_board_api.dto.payload.TaskPayloadDTO;
import com.karoldm.k_board_api.dto.response.TaskListResponseDTO;
import com.karoldm.k_board_api.dto.response.TaskResponseDTO;
import com.karoldm.k_board_api.entities.Project;
import com.karoldm.k_board_api.entities.Task;
import com.karoldm.k_board_api.entities.User;
import com.karoldm.k_board_api.enums.TaskStatus;
import com.karoldm.k_board_api.mappers.TaskMapper;
import com.karoldm.k_board_api.repositories.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

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

        task.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        task.setStatus(TaskStatus.PENDING.toString());

        task.setProject(project);
        task.setCreatedBy(createdBy);
        task.setResponsible(members);

        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }

    @Transactional
    protected void updateTaskData(Task task, EditTaskPayloadDTO data) {
        if(data.status().isPresent()){
            task.setStatus(data.status().get().toString());
        }
        if(data.description().isPresent()){
            task.setDescription(data.description().get());
        }
        if(data.title().isPresent()){
            task.setTitle(data.title().get());
        }
        if(data.color().isPresent()){
            task.setColor(data.color().get());
        }
        if(data.tags().isPresent()){
            task.setTags(data.tags().get());
        }
    }

    @Transactional
    public Task editTask(Task task, EditTaskPayloadDTO data, Set<User> responsible){
        this.updateTaskData(task, data);
        task.setResponsible(responsible);
        return taskRepository.save(task);
    }

    @Transactional
    public Task editTask(Task task, EditTaskPayloadDTO data){
        updateTaskData(task, data);
        return taskRepository.save(task);
    }


    public TaskListResponseDTO getTasksByProject(Project project, Optional<UUID> memberId) {
        List<Task> tasks;

        if (memberId.isPresent()) {
            tasks = taskRepository.findByProjectAndResponsibleContaining(project, memberId.get());
        } else {
            tasks = taskRepository.findByProject(project);
        }

        tasks.sort(Comparator.comparing(Task::getCreatedAt));

        List<Task> pending = tasks.stream().filter(task -> Objects.equals(task.getStatus(), TaskStatus.PENDING.toString())).toList();
        List<Task> doing = tasks.stream().filter(task -> Objects.equals(task.getStatus(), TaskStatus.DOING.toString())).toList();
        List<Task> completed = tasks.stream().filter(task -> Objects.equals(task.getStatus(), TaskStatus.COMPLETED.toString())).toList();

        return new TaskListResponseDTO(
                pending.stream().map(TaskMapper::toTaskResponseDTO).toList(),
                completed.stream().map(TaskMapper::toTaskResponseDTO).toList(),
                doing.stream().map(TaskMapper::toTaskResponseDTO).toList(),
                tasks.size(),
                pending.size(),
                doing.size(),
                completed.size()
        );
    }
}
