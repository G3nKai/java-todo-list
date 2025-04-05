package com.java.todolist.core.services;

import com.java.todolist.core.domain.Status;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.java.todolist.application.dto.TaskCreateDTO;
import com.java.todolist.core.domain.Task;
import com.java.todolist.core.repositories.ITaskRepository;

@Service
public class TaskService {

    @Autowired
    private ITaskRepository taskRepository;

    public List<Task> getAllTasks(String sortBy, String direction) {
        Sort sort = Sort.unsorted();
        
        if (sortBy != null && direction != null) {
            try {
                direction = direction.trim().toLowerCase();
                sortBy = sortBy.trim().toLowerCase();

                if (!isValidSortDirection(direction)) {
                    throw new IllegalArgumentException();
                }

                if (!isValidSortField(sortBy)) {
                    throw new IllegalArgumentException();
                }

    
                System.out.println("Sorting by: " + sortBy + " Direction: " + direction);
                Sort.Direction directionSort = Sort.Direction.fromString(direction);
                Sort.Order order = new Sort.Order(directionSort, sortBy);
                System.out.println(order);
                sort = Sort.by(order);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid sort field or direction: sortBy - " + sortBy + ", Direction - " + direction, e);
            }
        }

        System.out.println("Sort applied: " + sort);

        return taskRepository.findAll(sort);
    }

    private boolean isValidSortDirection(String direction) {
        return Arrays.asList("asc", "desc").contains(direction);
    }

    private boolean isValidSortField(String sortBy) {
        return Arrays.asList("name", "priority", "status", "created").contains(sortBy);
    }

    public Task createTask(TaskCreateDTO taskCreateDTO) {
        Status status = taskCreateDTO.getDeadline().isBefore(ZonedDateTime.now()) ? Status.Overdue : Status.Active;
        
        Task task = new Task(
            taskCreateDTO.getName(),
            taskCreateDTO.getDescription(),
            taskCreateDTO.getDeadline(),
            status,
            taskCreateDTO.getPriority(),
            ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS),
            null
        );
        return taskRepository.save(task);
    }

    public void deleteTask(UUID id) {
        taskRepository.findById(id)
                      .ifPresentOrElse(
                        _task -> taskRepository.deleteById(id), 
                        () -> {
                            throw new RuntimeException(String.format("Task with id = %s does not exist", id));
                        });
    }
}