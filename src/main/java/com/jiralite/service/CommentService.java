package com.jiralite.service;

import com.jiralite.domain.AuditEventType;
import com.jiralite.domain.Comment;
import com.jiralite.domain.Project;
import com.jiralite.domain.Task;
import com.jiralite.domain.User;
import com.jiralite.dto.CommentCreateRequest;
import com.jiralite.dto.CommentResponse;
import com.jiralite.exception.ResourceNotFoundException;
import com.jiralite.repository.CommentRepository;
import com.jiralite.repository.ProjectRepository;
import com.jiralite.repository.TaskRepository;
import com.jiralite.security.SecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final SecurityService securityService;
    private final AuditLogService auditLogService;

    public CommentService(CommentRepository commentRepository,
                          TaskRepository taskRepository,
                          ProjectRepository projectRepository,
                          SecurityService securityService,
                          AuditLogService auditLogService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.securityService = securityService;
        this.auditLogService = auditLogService;
    }

    public CommentResponse addComment(Long taskId, CommentCreateRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Project project = projectRepository.findByIdWithMembers(task.getProject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found for task"));

        // Any project member can add comments, including Viewers (and Admin manages everything)
        securityService.requireProjectMemberOrAdmin(project);

        User author = securityService.getCurrentUser();
        Comment comment = new Comment(task, author, request.getBody());
        Comment saved = commentRepository.save(comment);

        auditLogService.log(
                AuditEventType.COMMENT_ADDED,
                author.getUsername(),
                "COMMENT",
                saved.getId(),
                project.getId(),
                task.getId(),
                "Comment added to task: " + task.getTitle()
        );

        return CommentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Project project = projectRepository.findByIdWithMembers(task.getProject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found for task"));

        securityService.requireProjectMemberOrAdmin(project);

        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
