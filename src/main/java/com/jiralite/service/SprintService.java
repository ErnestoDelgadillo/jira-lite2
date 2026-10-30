package com.jiralite.service;

import com.jiralite.domain.AuditEventType;
import com.jiralite.domain.Project;
import com.jiralite.domain.Sprint;
import com.jiralite.domain.SprintStatus;
import com.jiralite.domain.User;
import com.jiralite.dto.SprintCreateRequest;
import com.jiralite.dto.SprintResponse;
import com.jiralite.exception.BadRequestException;
import com.jiralite.exception.ResourceNotFoundException;
import com.jiralite.repository.ProjectRepository;
import com.jiralite.repository.SprintRepository;
import com.jiralite.security.SecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final SecurityService securityService;
    private final AuditLogService auditLogService;

    public SprintService(SprintRepository sprintRepository,
                         ProjectRepository projectRepository,
                         SecurityService securityService,
                         AuditLogService auditLogService) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.securityService = securityService;
        this.auditLogService = auditLogService;
    }

    public SprintResponse createSprint(SprintCreateRequest request) {
        Project project = projectRepository.findByIdWithMembers(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));
        securityService.requireProjectManagerOrAdmin(project);

        Sprint sprint = new Sprint(
                project,
                request.getName(),
                request.getStartDate(),
                request.getEndDate(),
                SprintStatus.PLANNED
        );
        Sprint saved = sprintRepository.save(sprint);

        User actor = securityService.getCurrentUser();
        auditLogService.log(
                AuditEventType.SPRINT_CREATED,
                actor.getUsername(),
                "SPRINT",
                saved.getId(),
                project.getId(),
                null,
                "Sprint created: " + saved.getName()
        );

        return SprintResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public SprintResponse getSprint(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + sprintId));
        Project project = projectRepository.findByIdWithMembers(sprint.getProject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        securityService.requireProjectMemberOrAdmin(project);

        return SprintResponse.fromEntity(sprint);
    }

    @Transactional(readOnly = true)
    public List<SprintResponse> listSprints(Long projectId) {
        if (projectId != null) {
            Project project = projectRepository.findByIdWithMembers(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
            securityService.requireProjectMemberOrAdmin(project);
            return sprintRepository.findByProjectId(projectId).stream()
                    .map(SprintResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        securityService.requireAdmin();
        return sprintRepository.findAll().stream()
                .map(SprintResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public SprintResponse startSprint(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + sprintId));
        Project project = projectRepository.findByIdWithMembers(sprint.getProject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        securityService.requireProjectManagerOrAdmin(project);

        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new BadRequestException("Sprint cannot be started from status: " + sprint.getStatus());
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        Sprint updated = sprintRepository.save(sprint);

        User actor = securityService.getCurrentUser();
        auditLogService.log(
                AuditEventType.SPRINT_STARTED,
                actor.getUsername(),
                "SPRINT",
                updated.getId(),
                project.getId(),
                null,
                "Sprint started: " + updated.getName()
        );

        return SprintResponse.fromEntity(updated);
    }

    public SprintResponse completeSprint(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + sprintId));
        Project project = projectRepository.findByIdWithMembers(sprint.getProject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        securityService.requireProjectManagerOrAdmin(project);

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new BadRequestException("Sprint cannot be completed from status: " + sprint.getStatus());
        }

        sprint.setStatus(SprintStatus.COMPLETED);
        Sprint updated = sprintRepository.save(sprint);

        User actor = securityService.getCurrentUser();
        auditLogService.log(
                AuditEventType.SPRINT_COMPLETED,
                actor.getUsername(),
                "SPRINT",
                updated.getId(),
                project.getId(),
                null,
                "Sprint completed: " + updated.getName()
        );

        return SprintResponse.fromEntity(updated);
    }
}

