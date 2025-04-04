package com.java.todolist.core.services;

import com.java.todolist.core.domain.Status;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.java.todolist.application.dto.TaskCreateDTO;
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
        Status status = taskCreateDTO.getDeadline().isBefore(LocalDate.now()) ? Status.Overdue : Status.Active;
        
        Task task = new Task(
            taskCreateDTO.getName(),
            taskCreateDTO.getDescription(),
            taskCreateDTO.getDeadline(),
            status,
            taskCreateDTO.getPriority(),
            LocalDate.now(),
            null//уточнить
        );
        return taskRepository.save(task);
    }

    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }
}