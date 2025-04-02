package com.java.todolist.core.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.java.todolist.core.domain.Task;
import com.java.todolist.core.repositories.ITaskRepository;

@Service
public class TaskService {

    @Autowired
    private ITaskRepository taskRepository;

    public List<Task> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        System.out.println("Retrieved tasks: " + tasks);
        return tasks;
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }
}
