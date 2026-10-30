package com.jiralite.controller;

import com.jiralite.dto.AuditLogResponse;
import com.jiralite.exception.BadRequestException;
import com.jiralite.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<AuditLogResponse>> getProjectAuditLogsPath(@PathVariable("projectId") Long projectId) {
        return ResponseEntity.ok(auditLogService.getByProjectId(projectId));
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<List<AuditLogResponse>> getTaskAuditLogsPath(@PathVariable("taskId") Long taskId) {
        return ResponseEntity.ok(auditLogService.getByTaskId(taskId));
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            @RequestParam(value = "project", required = false) Long projectParam,
            @RequestParam(value = "projectId", required = false) Long projectIdParam,
            @RequestParam(value = "task", required = false) Long taskParam,
            @RequestParam(value = "taskId", required = false) Long taskIdParam) {

        Long effectiveProjectId = projectIdParam != null ? projectIdParam : projectParam;
        Long effectiveTaskId = taskIdParam != null ? taskIdParam : taskParam;

        if (effectiveProjectId != null) {
            return ResponseEntity.ok(auditLogService.getByProjectId(effectiveProjectId));
        }
        if (effectiveTaskId != null) {
            return ResponseEntity.ok(auditLogService.getByTaskId(effectiveTaskId));
        }

        throw new BadRequestException("Either projectId or taskId query parameter must be provided");
    }
}

