package com.jiralite.dto;

import com.jiralite.domain.Task;
import com.jiralite.domain.TaskPriority;
import com.jiralite.domain.TaskStatus;

import java.time.LocalDate;

public class TaskResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private UserResponse assignee;
    private UserResponse reporter;
    private Long sprintId;
    private String sprintName;
    private LocalDate dueDate;

    public TaskResponse() {
    }

    public TaskResponse(Long id, Long projectId, String projectName, String title, String description,
                        TaskStatus status, TaskPriority priority, UserResponse assignee, UserResponse reporter,
                        Long sprintId, String sprintName, LocalDate dueDate) {
        this.id = id;
        this.projectId = projectId;
        this.projectName = projectName;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.assignee = assignee;
        this.reporter = reporter;
        this.sprintId = sprintId;
        this.sprintName = sprintName;
        this.dueDate = dueDate;
    }

    public static TaskResponse fromEntity(Task task) {
        if (task == null) {
            return null;
        }
        Long pId = task.getProject() != null ? task.getProject().getId() : null;
        String pName = task.getProject() != null ? task.getProject().getName() : null;
        Long sId = task.getSprint() != null ? task.getSprint().getId() : null;
        String sName = task.getSprint() != null ? task.getSprint().getName() : null;
        return new TaskResponse(
                task.getId(),
                pId,
                pName,
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                UserResponse.fromEntity(task.getAssignee()),
                UserResponse.fromEntity(task.getReporter()),
                sId,
                sName,
                task.getDueDate()
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

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public UserResponse getAssignee() {
        return assignee;
    }

    public void setAssignee(UserResponse assignee) {
        this.assignee = assignee;
    }

    public UserResponse getReporter() {
        return reporter;
    }

    public void setReporter(UserResponse reporter) {
        this.reporter = reporter;
    }

    public Long getSprintId() {
        return sprintId;
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }

    public String getSprintName() {
        return sprintName;
    }

    public void setSprintName(String sprintName) {
        this.sprintName = sprintName;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
