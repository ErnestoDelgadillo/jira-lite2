package com.jiralite.controller;

import com.jiralite.domain.TaskPriority;
import com.jiralite.domain.TaskStatus;
import com.jiralite.dto.TaskCreateRequest;
import com.jiralite.dto.TaskResponse;
import com.jiralite.dto.TaskTransitionRequest;
import com.jiralite.dto.TaskUpdateRequest;
import com.jiralite.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskCreateRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable("id") Long id) {
        return ResponseEntity.ok(taskService.getTask(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable("id") Long id,
                                                   @Valid @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @PostMapping("/{id}/transition")
    public ResponseEntity<TaskResponse> transitionTask(@PathVariable("id") Long id,
                                                       @Valid @RequestBody TaskTransitionRequest request) {
        return ResponseEntity.ok(taskService.transitionStatus(id, request.getStatus()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> transitionTaskPatch(@PathVariable("id") Long id,
                                                            @Valid @RequestBody TaskTransitionRequest request) {
        return ResponseEntity.ok(taskService.transitionStatus(id, request.getStatus()));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listTasks(
            @RequestParam(value = "project", required = false) Long projectParam,
            @RequestParam(value = "projectId", required = false) Long projectIdParam,
            @RequestParam(value = "status", required = false) TaskStatus status,
            @RequestParam(value = "priority", required = false) TaskPriority priority,
            @RequestParam(value = "assignee", required = false) Long assigneeParam,
            @RequestParam(value = "assigneeId", required = false) Long assigneeIdParam,
            @RequestParam(value = "sprint", required = false) Long sprintParam,
            @RequestParam(value = "sprintId", required = false) Long sprintIdParam) {

        Long effectiveProjectId = projectIdParam != null ? projectIdParam : projectParam;
        Long effectiveAssigneeId = assigneeIdParam != null ? assigneeIdParam : assigneeParam;
        Long effectiveSprintId = sprintIdParam != null ? sprintIdParam : sprintParam;

        List<TaskResponse> tasks = taskService.getTasks(
                effectiveProjectId,
                status,
                priority,
                effectiveAssigneeId,
                effectiveSprintId
        );
        return ResponseEntity.ok(tasks);
    }
}
