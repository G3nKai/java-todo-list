package com.java.todolist.application.dto;

import java.time.ZonedDateTime;

import com.java.todolist.core.domain.Priority;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TaskCreateDTO {
    @NotNull(message = "Name can not be null")
    @Size(min = 4, message = "Name should be at least 4 characters long")
    private String name;
    private String description;
    @NotNull(message = "Deadline can not be null")
    private ZonedDateTime deadline;
    @NotNull(message = "Priority can not be null")
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