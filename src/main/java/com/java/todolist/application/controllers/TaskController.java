package com.java.todolist.application.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.java.todolist.application.dto.TaskCreateDTO;
import com.java.todolist.application.dto.TaskPutDTO;
import com.java.todolist.core.domain.Task;
import com.java.todolist.core.services.TaskService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("")
    public List<Task> getAllTasks(
        @RequestParam(value = "sortBy", defaultValue = "created") String sortBy,
        @RequestParam(value = "direction", defaultValue = "DESC") String direction) {
        return taskService.getAllTasks(sortBy, direction);
    }

    @GetMapping("/{id}")
    public Task getTask(@PathVariable UUID id) {
        return taskService.getTask(id);
    }

    @PostMapping("")
    public Task createTask(@RequestBody @Valid TaskCreateDTO taskCreateDTO) {
        return taskService.createTask(taskCreateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
    }

    @PatchMapping("/{id}")
    public Task completeTask(@PathVariable UUID id) {
        return taskService.completeTask(id);
    }

    @PutMapping("/{id}")
    public Task putMethodName(@PathVariable UUID id, @RequestBody @Valid TaskPutDTO taskPutDTO) {
        return taskService.putTask(id, taskPutDTO);
    }
}
