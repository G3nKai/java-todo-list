package com.java.todolist.application.dto;

import jakarta.validation.constraints.NotNull;

public class TaskCreateDTO {
    @NotNull
    private String name;
    @NotNull
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
