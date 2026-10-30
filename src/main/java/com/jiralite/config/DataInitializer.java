package com.jiralite.config;

import com.jiralite.domain.*;
import com.jiralite.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ProjectRepository projectRepository,
                           SprintRepository sprintRepository,
                           TaskRepository taskRepository,
                           CommentRepository commentRepository,
                           AuditLogRepository auditLogRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.taskRepository = taskRepository;
        this.commentRepository = commentRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User("admin", passwordEncoder.encode("admin123"), Role.ADMIN);
            User manager = new User("manager", passwordEncoder.encode("manager123"), Role.MANAGER);
            User developer = new User("developer", passwordEncoder.encode("developer123"), Role.DEVELOPER);
            User viewer = new User("viewer", passwordEncoder.encode("viewer123"), Role.VIEWER);

            userRepository.save(admin);
            userRepository.save(manager);
            userRepository.save(developer);
            userRepository.save(viewer);

            // Seed initial sample Project
            Project project = new Project("Jira Lite System", "Primary task tracking workspace for development and operations");
            project.addMember(manager);
            project.addMember(developer);
            project.addMember(viewer);
            project.addMember(admin);
            Project savedProject = projectRepository.save(project);

            auditLogRepository.save(new AuditLog(
                    AuditEventType.PROJECT_CREATED,
                    "manager",
                    "PROJECT",
                    savedProject.getId(),
                    savedProject.getId(),
                    null,
                    "Project created: Jira Lite System",
                    Instant.now().minusSeconds(86400 * 3)
            ));

            // Seed Sprints
            Sprint sprint1 = new Sprint(
                    savedProject,
                    "Sprint 1 - Core Services",
                    LocalDate.now().minusDays(5),
                    LocalDate.now().plusDays(9),
                    SprintStatus.ACTIVE
            );
            Sprint savedSprint1 = sprintRepository.save(sprint1);

            Sprint sprint2 = new Sprint(
                    savedProject,
                    "Sprint 2 - Scaling & Performance",
                    LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(24),
                    SprintStatus.PLANNED
            );
            sprintRepository.save(sprint2);

            auditLogRepository.save(new AuditLog(
                    AuditEventType.SPRINT_STARTED,
                    "manager",
                    "SPRINT",
                    savedSprint1.getId(),
                    savedProject.getId(),
                    null,
                    "Sprint started: Sprint 1 - Core Services",
                    Instant.now().minusSeconds(86400 * 2)
            ));

            // Seed Tasks across different statuses
            Task task1 = new Task(
                    savedProject,
                    "Setup Spring Security JWT authentication",
                    "Implement stateless token-based authorization and role checks",
                    TaskStatus.DONE,
                    TaskPriority.CRITICAL,
                    developer,
                    manager,
                    savedSprint1,
                    LocalDate.now().plusDays(2)
            );
            Task savedTask1 = taskRepository.save(task1);

            Task task2 = new Task(
                    savedProject,
                    "Implement Kanban board UI",
                    "Interactive drag/transition board with columns for OPEN, IN_PROGRESS, IN_REVIEW, DONE, CLOSED",
                    TaskStatus.IN_PROGRESS,
                    TaskPriority.HIGH,
                    developer,
                    manager,
                    savedSprint1,
                    LocalDate.now().plusDays(3)
            );
            Task savedTask2 = taskRepository.save(task2);

            Task task3 = new Task(
                    savedProject,
                    "Audit log timeline review",
                    "Verify all lifecycle state transitions are logged with typed event enum",
                    TaskStatus.IN_REVIEW,
                    TaskPriority.MEDIUM,
                    developer,
                    manager,
                    savedSprint1,
                    LocalDate.now().plusDays(4)
            );
            Task savedTask3 = taskRepository.save(task3);

            Task task4 = new Task(
                    savedProject,
                    "Automated integration test coverage",
                    "Write complete MockMvc end-to-end tests for all endpoints and permission boundaries",
                    TaskStatus.OPEN,
                    TaskPriority.HIGH,
                    developer,
                    manager,
                    savedSprint1,
                    LocalDate.now().plusDays(6)
            );
            Task savedTask4 = taskRepository.save(task4);

            Task task5 = new Task(
                    savedProject,
                    "Sprint retrospective and planning",
                    "Review completed tasks and prioritize Sprint 2 backlog items",
                    TaskStatus.CLOSED,
                    TaskPriority.LOW,
                    manager,
                    manager,
                    savedSprint1,
                    LocalDate.now().minusDays(1)
            );
            Task savedTask5 = taskRepository.save(task5);

            // Seed initial comments
            Comment comment1 = new Comment(
                    savedTask2,
                    developer,
                    "Working on the task transition buttons and filtering panel."
            );
            commentRepository.save(comment1);

            Comment comment2 = new Comment(
                    savedTask2,
                    viewer,
                    "Looking great! Ensure viewers can still read and post feedback comments."
            );
            commentRepository.save(comment2);

            // Seed task audit logs
            auditLogRepository.save(new AuditLog(
                    AuditEventType.TASK_CREATED,
                    "manager",
                    "TASK",
                    savedTask2.getId(),
                    savedProject.getId(),
                    savedTask2.getId(),
                    "Task created: Implement Kanban board UI",
                    Instant.now().minusSeconds(7200)
            ));

            auditLogRepository.save(new AuditLog(
                    AuditEventType.TASK_STATUS_CHANGED,
                    "developer",
                    "TASK",
                    savedTask2.getId(),
                    savedProject.getId(),
                    savedTask2.getId(),
                    "Status changed from OPEN to IN_PROGRESS",
                    Instant.now().minusSeconds(3600)
            ));

            auditLogRepository.save(new AuditLog(
                    AuditEventType.COMMENT_ADDED,
                    "viewer",
                    "COMMENT",
                    comment2.getId(),
                    savedProject.getId(),
                    savedTask2.getId(),
                    "Comment added by viewer",
                    Instant.now().minusSeconds(1800)
            ));
        }
    }
}
