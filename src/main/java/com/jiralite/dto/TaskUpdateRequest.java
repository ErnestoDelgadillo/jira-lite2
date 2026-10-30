package com.jiralite.dto;

import com.jiralite.domain.TaskPriority;
import java.time.LocalDate;

public class TaskUpdateRequest {

    private String title;
    private String description;
    private TaskPriority priority;
    private Long assigneeId;
    private Long sprintId;
    private LocalDate dueDate;

    public TaskUpdateRequest() {
    }

    public TaskUpdateRequest(String title, String description, TaskPriority priority,
                              Long assigneeId, Long sprintId, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.assigneeId = assigneeId;
        this.sprintId = sprintId;
        this.dueDate = dueDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Long getSprintId() {
        return sprintId;
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
