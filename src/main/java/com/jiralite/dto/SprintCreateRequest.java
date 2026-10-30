package com.jiralite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class SprintCreateRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Sprint name cannot be blank")
    private String name;

    private LocalDate startDate;
    private LocalDate endDate;

    public SprintCreateRequest() {
    }

    public SprintCreateRequest(Long projectId, String name, LocalDate startDate, LocalDate endDate) {
        this.projectId = projectId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
