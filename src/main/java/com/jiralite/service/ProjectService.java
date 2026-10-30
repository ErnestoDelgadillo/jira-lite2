package com.jiralite.service;

import com.jiralite.domain.AuditEventType;
import com.jiralite.domain.Project;
import com.jiralite.domain.Role;
import com.jiralite.domain.User;
import com.jiralite.dto.ProjectCreateRequest;
import com.jiralite.dto.ProjectResponse;
import com.jiralite.exception.BadRequestException;
import com.jiralite.exception.ResourceNotFoundException;
import com.jiralite.repository.ProjectRepository;
import com.jiralite.repository.UserRepository;
import com.jiralite.security.SecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final AuditLogService auditLogService;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          SecurityService securityService,
                          AuditLogService auditLogService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.securityService = securityService;
        this.auditLogService = auditLogService;
    }

    public ProjectResponse createProject(ProjectCreateRequest request) {
        securityService.requireManagerOrAdmin();
        User currentUser = securityService.getCurrentUser();

        Project project = new Project(request.getName(), request.getDescription());
        project.addMember(currentUser);
        Project saved = projectRepository.save(project);

        auditLogService.log(
                AuditEventType.PROJECT_CREATED,
                currentUser.getUsername(),
                "PROJECT",
                saved.getId(),
                saved.getId(),
                null,
                "Project created: " + saved.getName()
        );

        return ProjectResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects() {
        User currentUser = securityService.getCurrentUser();
        List<Project> projects;
        if (currentUser.getRole() == Role.ADMIN) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findByMembersContaining(currentUser);
        }

        return projects.stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long id) {
        Project project = projectRepository.findByIdWithMembers(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        securityService.requireProjectMemberOrAdmin(project);
        return ProjectResponse.fromEntity(project);
    }

    public ProjectResponse addMember(Long projectId, Long userId) {
        Project project = projectRepository.findByIdWithMembers(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        securityService.requireProjectManagerOrAdmin(project);

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (securityService.isMember(project, userToAdd)) {
            throw new BadRequestException("User is already a member of this project");
        }

        project.addMember(userToAdd);
        Project updated = projectRepository.save(project);

        User actor = securityService.getCurrentUser();
        auditLogService.log(
                AuditEventType.PROJECT_MEMBER_ADDED,
                actor.getUsername(),
                "PROJECT",
                updated.getId(),
                updated.getId(),
                null,
                "Added user " + userToAdd.getUsername() + " to project " + updated.getName()
        );

        return ProjectResponse.fromEntity(updated);
    }

    public ProjectResponse removeMember(Long projectId, Long userId) {
        Project project = projectRepository.findByIdWithMembers(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        securityService.requireProjectManagerOrAdmin(project);

        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!securityService.isMember(project, userToRemove)) {
            throw new BadRequestException("User is not a member of this project");
        }

        project.removeMember(userToRemove);
        Project updated = projectRepository.save(project);

        User actor = securityService.getCurrentUser();
        auditLogService.log(
                AuditEventType.PROJECT_MEMBER_REMOVED,
                actor.getUsername(),
                "PROJECT",
                updated.getId(),
                updated.getId(),
                null,
                "Removed user " + userToRemove.getUsername() + " from project " + updated.getName()
        );

        return ProjectResponse.fromEntity(updated);
    }
}

