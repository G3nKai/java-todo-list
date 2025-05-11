package com.java.todolist.application.dto;

import java.time.ZonedDateTime;

import com.java.todolist.core.domain.Priority;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TaskCreateDTO {
    @NotNull(message = "Name can not be null")
    @Size(min = 4, message = "Name should be at least 4 characters long")
    @Size(max = 255, message = "Name should be no more than 255 characters long")
    private String name;
    @Size(max = 2047, message = "Description can not be more then 2047 characters long")
    private String description;
    private ZonedDateTime deadline;
    private Priority priority;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ZonedDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(ZonedDateTime deadline) {
        this.deadline = deadline;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}