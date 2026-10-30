package com.jiralite.dto;

import com.jiralite.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;

public class TaskTransitionRequest {

    @NotNull(message = "Target status is required")
    private TaskStatus status;

    public TaskTransitionRequest() {
    }

    public TaskTransitionRequest(TaskStatus status) {
        this.status = status;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}

