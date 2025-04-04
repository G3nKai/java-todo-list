package com.java.todolist.core.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.java.todolist.application.dto.TaskCreateDTO;
import com.java.todolist.core.domain.Priority;
import com.java.todolist.core.domain.Status;
import com.java.todolist.core.domain.Task;
import com.java.todolist.core.repositories.ITaskRepository;

@Service
public class TaskService {

    @Autowired
    private ITaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task createTask(TaskCreateDTO taskCreateDTO) {
        Task task = new Task(
            taskCreateDTO.getName(),
            taskCreateDTO.getDescription(),
            taskCreateDTO.getDeadline(),
            (!taskCreateDTO.getStatus().equals(Status.Late) &&
                !taskCreateDTO.getStatus().equals(Status.Overdue) && 
                !taskCreateDTO.getStatus().equals(Status.Completed)) ? 
                taskCreateDTO.getStatus() : Status.Active,
            taskCreateDTO.getPriority() == null ? Priority.Medium : taskCreateDTO.getPriority()
        );
        return taskRepository.save(task);
    }

    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }
}