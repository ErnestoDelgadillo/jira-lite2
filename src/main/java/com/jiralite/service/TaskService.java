package com.jiralite.service;

import com.jiralite.domain.*;
import com.jiralite.dto.TaskCreateRequest;
import com.jiralite.dto.TaskResponse;
import com.jiralite.dto.TaskUpdateRequest;
import com.jiralite.exception.AccessDeniedException;
import com.jiralite.exception.BadRequestException;
import com.jiralite.exception.InvalidTransitionException;
import com.jiralite.exception.ResourceNotFoundException;
import com.jiralite.repository.ProjectRepository;
import com.jiralite.repository.SprintRepository;
import com.jiralite.repository.TaskRepository;
import com.jiralite.repository.UserRepository;
import com.jiralite.security.SecurityService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SprintRepository sprintRepository;
    private final SecurityService securityService;
    private final AuditLogService auditLogService;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       SprintRepository sprintRepository,
                       SecurityService securityService,
                       AuditLogService auditLogService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.sprintRepository = sprintRepository;
        this.securityService = securityService;
        this.auditLogService = auditLogService;
    }

    public TaskResponse createTask(TaskCreateRequest request) {
        Project project = projectRepository.findByIdWithMembers(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));
        securityService.requireProjectMemberOrAdmin(project);
        securityService.requireNotViewer();

        User reporter = securityService.getCurrentUser();

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.getAssigneeId()));
        }

        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + request.getSprintId()));
            if (!sprint.getProject().getId().equals(project.getId())) {
                throw new BadRequestException("Sprint does not belong to the task's project");
            }
        }

        Task task = new Task(
                project,
                request.getTitle(),
                request.getDescription(),
                TaskStatus.OPEN,
                request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM,
                assignee,
                reporter,
                sprint,
                request.getDueDate()
        );
        Task saved = taskRepository.save(task);

        auditLogService.log(
                AuditEventType.TASK_CREATED,
                reporter.getUsername(),
                "TASK",
                saved.getId(),
                project.getId(),
                saved.getId(),
                "Task created: " + saved.getTitle()
        );

        return TaskResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        Project project = projectRepository.findByIdWithMembers(task.getProject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found for task"));
        securityService.requireProjectMemberOrAdmin(project);

        return TaskResponse.fromEntity(task);
    }

    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        Project project = projectRepository.findByIdWithMembers(task.getProject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found for task"));
        securityService.requireProjectMemberOrAdmin(project);
        securityService.requireNotViewer();

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.getAssigneeId()));
            task.setAssignee(assignee);
        }

        if (request.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id: " + request.getSprintId()));
            if (!sprint.getProject().getId().equals(task.getProject().getId())) {
                throw new BadRequestException("Sprint does not belong to the task's project");
            }
            task.setSprint(sprint);
        }

        Task updated = taskRepository.save(task);
        User actor = securityService.getCurrentUser();
        auditLogService.log(
                AuditEventType.TASK_UPDATED,
                actor.getUsername(),
                "TASK",
                updated.getId(),
                project.getId(),
                updated.getId(),
                "Task updated: " + updated.getTitle()
        );

        return TaskResponse.fromEntity(updated);
    }

    public TaskResponse transitionStatus(Long taskId, TaskStatus targetStatus) {
        if (targetStatus == null) {
            throw new BadRequestException("Target status is required");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        Project project = projectRepository.findByIdWithMembers(task.getProject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found for task"));
        securityService.requireProjectMemberOrAdmin(project);

        User currentUser = securityService.getCurrentUser();
        TaskStatus currentStatus = task.getStatus();

        if (currentStatus == targetStatus) {
            return TaskResponse.fromEntity(task);
        }

        validateWorkflowTransition(currentStatus, targetStatus, currentUser);

        task.setStatus(targetStatus);
        Task updated = taskRepository.save(task);

        auditLogService.log(
                AuditEventType.TASK_STATUS_CHANGED,
                currentUser.getUsername(),
                "TASK",
                updated.getId(),
                project.getId(),
                updated.getId(),
                "Status changed from " + currentStatus + " to " + targetStatus
        );

        return TaskResponse.fromEntity(updated);
    }

    private void validateWorkflowTransition(TaskStatus current, TaskStatus target, User user) {
        // Reopening from CLOSED back to OPEN: only Managers and Admins
        if (current == TaskStatus.CLOSED && target == TaskStatus.OPEN) {
            if (!securityService.isManagerOrAdmin(user)) {
                throw new AccessDeniedException("Tasks can only be reopened from CLOSED back to OPEN by Managers and Admins");
            }
            return;
        }

        // Closing: only Managers and Admins can close a task
        if (current == TaskStatus.DONE && target == TaskStatus.CLOSED) {
            if (!securityService.isManagerOrAdmin(user)) {
                throw new AccessDeniedException("Only Managers and Admins can close a task");
            }
            return;
        }

        // Standard forward steps: OPEN -> IN_PROGRESS -> IN_REVIEW -> DONE
        boolean isForwardStep =
                (current == TaskStatus.OPEN && target == TaskStatus.IN_PROGRESS) ||
                (current == TaskStatus.IN_PROGRESS && target == TaskStatus.IN_REVIEW) ||
                (current == TaskStatus.IN_REVIEW && target == TaskStatus.DONE);

        if (isForwardStep) {
            if (user.getRole() == Role.VIEWER) {
                throw new AccessDeniedException("Viewers cannot transition tasks");
            }
            return;
        }

        throw new InvalidTransitionException("Invalid task status transition from " + current + " to " + target);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(Long projectId,
                                       TaskStatus status,
                                       TaskPriority priority,
                                       Long assigneeId,
                                       Long sprintId) {
        User currentUser = securityService.getCurrentUser();

        if (projectId != null) {
            Project project = projectRepository.findByIdWithMembers(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
            securityService.requireProjectMemberOrAdmin(project);

            Specification<Task> spec = TaskSpecification.filter(projectId, status, priority, assigneeId, sprintId);
            return taskRepository.findAll(spec).stream()
                    .map(TaskResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        // If no projectId specified: Admin can view across all, otherwise user can only view their projects' tasks
        Specification<Task> spec = TaskSpecification.filter(null, status, priority, assigneeId, sprintId);
        List<Task> allMatching = taskRepository.findAll(spec);

        if (currentUser.getRole() == Role.ADMIN) {
            return allMatching.stream()
                    .map(TaskResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        return allMatching.stream()
                .filter(task -> securityService.isMember(task.getProject(), currentUser))
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
    }
}

