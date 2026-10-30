package com.jiralite.service;

import com.jiralite.domain.AuditEventType;
import com.jiralite.domain.AuditLog;
import com.jiralite.domain.Project;
import com.jiralite.domain.Task;
import com.jiralite.dto.AuditLogResponse;
import com.jiralite.exception.ResourceNotFoundException;
import com.jiralite.repository.AuditLogRepository;
import com.jiralite.repository.ProjectRepository;
import com.jiralite.repository.TaskRepository;
import com.jiralite.security.SecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final SecurityService securityService;

    public AuditLogService(AuditLogRepository auditLogRepository,
                           ProjectRepository projectRepository,
                           TaskRepository taskRepository,
                           SecurityService securityService) {
        this.auditLogRepository = auditLogRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.securityService = securityService;
    }

    public void log(AuditEventType eventType, String actor, String entityType, Long entityId,
                    Long projectId, Long taskId, String details) {
        AuditLog auditLog = new AuditLog(eventType, actor, entityType, entityId, projectId, taskId, details, Instant.now());
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByProjectId(Long projectId) {
        Project project = projectRepository.findByIdWithMembers(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        securityService.requireProjectMemberOrAdmin(project);

        return auditLogRepository.findByProjectIdOrderByTimestampDesc(projectId).stream()
                .map(AuditLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByTaskId(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        Project project = projectRepository.findByIdWithMembers(task.getProject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found for task"));
        securityService.requireProjectMemberOrAdmin(project);

        return auditLogRepository.findByTaskIdOrderByTimestampDesc(taskId).stream()
                .map(AuditLogResponse::fromEntity)
                .collect(Collectors.toList());
    }
}

