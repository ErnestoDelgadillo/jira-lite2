package com.jiralite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectCreateRequest {

    @NotBlank(message = "Project name cannot be blank")
    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
    private String name;

    private String description;

    public ProjectCreateRequest() {
    }

    public ProjectCreateRequest(String name, String description) {
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
