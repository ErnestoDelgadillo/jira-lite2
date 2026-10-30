package com.jiralite.dto;

import com.jiralite.domain.Sprint;
import com.jiralite.domain.SprintStatus;

import java.time.LocalDate;

public class SprintResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private SprintStatus status;

    public SprintResponse() {
    }

    public SprintResponse(Long id, Long projectId, String projectName, String name,
                          LocalDate startDate, LocalDate endDate, SprintStatus status) {
        this.id = id;
        this.projectId = projectId;
        this.projectName = projectName;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public static SprintResponse fromEntity(Sprint sprint) {
        if (sprint == null) {
            return null;
        }
        Long pId = sprint.getProject() != null ? sprint.getProject().getId() : null;
        String pName = sprint.getProject() != null ? sprint.getProject().getName() : null;
        return new SprintResponse(
                sprint.getId(),
                pId,
                pName,
                sprint.getName(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprint.getStatus()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public SprintStatus getStatus() {
        return status;
    }

    public void setStatus(SprintStatus status) {
        this.status = status;
    }
}

