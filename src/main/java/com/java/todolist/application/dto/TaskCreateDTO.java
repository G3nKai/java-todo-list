package com.java.todolist.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TaskCreateDTO {
    @NotNull(message = "Name can not be null.")
    @Size(min = 4, message = "Name should be at least 4 characters long.")
    private String name;
    private String description;

    public TaskCreateDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }

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
}
