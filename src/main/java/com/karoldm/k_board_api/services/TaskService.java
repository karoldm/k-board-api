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
import com.karoldm.k_board_api.repositories.ProjectRepository;
import com.karoldm.k_board_api.repositories.TaskRepository;
import com.karoldm.k_board_api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final AuthService authService;

    public Optional<Task> findTaskById(UUID id) {
        return taskRepository.findById(id);
    }

    @Transactional
    public TaskResponseDTO createTask(TaskPayloadDTO data){
        User user = authService.getSessionUser();

        checkProjectOwnershipOrParticipation(data.projectId());
        Project project = getProjectOrThrow(data.projectId());

        Set<UUID> membersId = data.membersId();
        Set<User> members = new HashSet<>(userRepository.findAllById(membersId));

        Set<UUID> missingUserIds = membersId.stream()
                .filter(memberId -> members.stream().noneMatch(member -> member.getId().equals(memberId)))
                .collect(Collectors.toSet());

        if (!missingUserIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Users not found with IDs: " + missingUserIds);
        }

        Task task = Task.builder()
                .tags(data.tags())
                .description(data.description())
                .title(data.title())
                .color(data.color())
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .status(TaskStatus.PENDING.toString())
                .project(project)
                .createdBy(user)
                .responsible(members)
                .build();

        Task savedTask = taskRepository.save(task);

        return TaskMapper.toTaskResponseDTO(savedTask);
    }

    @Transactional
    public void deleteTask(UUID id) {
        Optional<Task> task = findTaskById(id);

        if(task.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + id);
        }

        checkProjectOwnershipOrParticipation(task.get().getProject().getId());

        taskRepository.deleteById(id);
    }

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
    public TaskResponseDTO editTask(EditTaskPayloadDTO data, UUID taskId) {
        Optional<Task> optionalTask = findTaskById(taskId);

        if (optionalTask.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + taskId);
        }

        Task task = optionalTask.get();

        checkProjectOwnershipOrParticipation(task.getProject().getId());

        if (data.responsible().isPresent()) {
            Project project = task.getProject();
            Set<User> projectMembers = new HashSet<>(project.getMembers());
            projectMembers.add(project.getOwner());

            // handle user id doest not belong to project
            Set<UUID> missingUserIds = data.responsible().get().stream()
                    .filter(memberId -> projectMembers.stream().noneMatch(user -> user.getId().equals(memberId)))
                    .collect(Collectors.toSet());

            if (!missingUserIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Users not found with ID's: " + missingUserIds);
            }

            // get users by id
            Set<User> responsible = new HashSet<>(userRepository.findAllById(data.responsible().get()));
            task.setResponsible(responsible);
        }

        this.updateTaskData(task, data);

        Task savedTask = taskRepository.save(task);

        return TaskMapper.toTaskResponseDTO(savedTask);
    }

    @Transactional
    public Task editTask(Task task, EditTaskPayloadDTO data){
        updateTaskData(task, data);
        return taskRepository.save(task);
    }


    public TaskListResponseDTO getTasksByProject(UUID projectId, Optional<UUID> memberId) {
        checkProjectOwnershipOrParticipation(projectId);
        Project project = getProjectOrThrow(projectId);

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

    private void checkProjectOwnershipOrParticipation(UUID projectId) {
        boolean isOwner = authService.getSessionUser().getProjects().stream().anyMatch(project -> project.getId().equals(projectId));
        boolean isMember = authService.getSessionUser().getParticipatedProjects().stream().anyMatch(project -> project.getId().equals(projectId));
        if (!isOwner && !isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this project");
        }
    }

    private Project getProjectOrThrow(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));
    }
}
