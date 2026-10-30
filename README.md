# Jira Lite

A clean, robust Jira-like task management backend built with Spring Boot 3.3.5 and Java 21.

## Tech Stack
- **Java 21**
- **Spring Boot 3.3.5** with Maven
- **H2 in-memory database** (`jdbc:h2:mem:jiralitedb`)
- **Spring Security + OAuth2 Resource Server / JWT** (HMAC-SHA256)
- **Spring Data JPA**
- Plain Java (zero Lombok, zero MapStruct, zero extra libraries)

---

## Architectural Layering

The application strictly follows clean architectural boundaries:

1. **Controllers** (`com.jiralite.controller`)
   - Handle HTTP: parse and validate requests with `@Valid`.
   - Forward calls to the service layer.
   - Return appropriate HTTP response codes (`201 Created`, `200 OK`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`).
   - No business logic, no role/authorization checks, and no filtering in controllers.

2. **Services** (`com.jiralite.service`)
   - Contain all business logic and workflow validation.
   - Authorization is enforced centrally using `SecurityService` across all services.
   - Dynamic JPA filtering (`TaskSpecification`) lives here.
   - Transaction boundaries (`@Transactional`) are defined here.
   - Records state changes to the `AuditLog`.

3. **Repositories** (`com.jiralite.repository`)
   - Data access only using Spring Data JPA (`JpaRepository` and `JpaSpecificationExecutor`).

---

## Domain Model & Roles

### Roles
- `ADMIN`: Manages everything (can create/delete/modify any resource, change user roles).
- `MANAGER`: Runs projects, creates sprints, and closes/reopens tasks.
- `DEVELOPER`: Works on tasks, updates status through development stages, adds comments.
- `VIEWER`: Read-only access to projects and tasks; can add comments.

### Task Workflow
```
OPEN ──> IN_PROGRESS ──> IN_REVIEW ──> DONE ──> CLOSED
  ▲                                               │
  └────────────────── (Reopen) ───────────────────┘
```
- **Forward Workflow**: `OPEN` → `IN_PROGRESS` → `IN_REVIEW` → `DONE`. Allowed for Developers, Managers, and Admins.
- **Closing a Task**: `DONE` → `CLOSED`. Allowed **only** for Managers and Admins.
- **Reopening a Task**: `CLOSED` → `OPEN`. Allowed **only** for Managers and Admins.
- **Invalid Transitions**: Any jump or backward move outside the above (e.g., `OPEN` → `CLOSED`, `IN_PROGRESS` → `CLOSED`, `DONE` → `IN_PROGRESS`) is rejected with HTTP `400 Bad Request`.

### Sprints
- Lifecycle: `PLANNED` → `ACTIVE` → `COMPLETED`.
- Only Managers (of that project) and Admins can start or complete sprints.

### Comments
- Any project member, including Viewers, can add comments to a task.

### Audit Log
Typed enum events (`PROJECT_CREATED`, `PROJECT_MEMBER_ADDED`, `PROJECT_MEMBER_REMOVED`, `SPRINT_CREATED`, `SPRINT_STARTED`, `SPRINT_COMPLETED`, `TASK_CREATED`, `TASK_UPDATED`, `TASK_STATUS_CHANGED`, `COMMENT_ADDED`, `USER_ROLE_CHANGED`, `USER_REGISTERED`) recording actor, entity reference, timestamp, and queryable per project and per task.

---

## Pre-seeded Users (Local Development)

On initial startup, default accounts are automatically created:
- `admin` / `admin123` (`ADMIN`)
- `manager` / `manager123` (`MANAGER`)
- `developer` / `developer123` (`DEVELOPER`)
- `viewer` / `viewer123` (`VIEWER`)

---

## Running the Application

Using Maven:
```powershell
mvn spring-boot:run
```
Or with the provided wrapper:
```powershell
.\mvn.cmd spring-boot:run
```

Running tests:
```powershell
.\mvn.cmd test
```

---

## API Reference

### 1. Authentication
- `POST /api/auth/register`
  ```json
  { "username": "jane", "password": "password123", "role": "DEVELOPER" }
  ```
- `POST /api/auth/login`
  ```json
  { "username": "admin", "password": "admin123" }
  ```
  Returns:
  ```json
  { "token": "<JWT_TOKEN>", "type": "Bearer", "id": 1, "username": "admin", "role": "ADMIN" }
  ```

### 2. Users
- `GET /api/users` (List all users)
- `PUT /api/users/{id}/role` (Change role - Admin only)
  ```json
  { "role": "MANAGER" }
  ```

### 3. Projects
- `POST /api/projects` (Create project - Manager or Admin)
  ```json
  { "name": "Mobile App", "description": "iOS and Android client" }
  ```
- `GET /api/projects` (List projects for current user or all if Admin)
- `GET /api/projects/{id}` (Get project details)
- `POST /api/projects/{id}/members` (Add project member)
  ```json
  { "userId": 3 }
  ```
- `DELETE /api/projects/{id}/members/{userId}` (Remove project member)

### 4. Sprints
- `POST /api/sprints` (Create sprint - Manager or Admin)
  ```json
  { "projectId": 1, "name": "Sprint 1", "startDate": "2026-11-01", "endDate": "2026-11-14" }
  ```
- `GET /api/sprints/{id}` (Get sprint details)
- `GET /api/sprints?projectId={id}` (List sprints for a project)
- `POST /api/sprints/{id}/start` (Start sprint: PLANNED → ACTIVE)
- `POST /api/sprints/{id}/complete` (Complete sprint: ACTIVE → COMPLETED)

### 5. Tasks
- `POST /api/tasks` (Create task)
  ```json
  {
    "projectId": 1,
    "title": "Build Auth API",
    "description": "Implement JWT auth",
    "priority": "HIGH",
    "assigneeId": 3,
    "sprintId": 1,
    "dueDate": "2026-11-05"
  }
  ```
- `GET /api/tasks/{id}` (Get task details)
- `PUT /api/tasks/{id}` (Update task fields)
- `POST /api/tasks/{id}/transition` (Transition status)
  ```json
  { "status": "IN_PROGRESS" }
  ```
- `GET /api/tasks?projectId=1&status=OPEN&priority=HIGH&assigneeId=3&sprintId=1` (List with filters)

### 6. Comments
- `POST /api/tasks/{taskId}/comments` (Add comment - Any project member including Viewers)
  ```json
  { "body": "PR is ready for review." }
  ```
- `GET /api/tasks/{taskId}/comments` (List comments on a task)

### 7. Audit Log
- `GET /api/audit-logs/projects/{projectId}` (Audit logs for a project)
- `GET /api/audit-logs/tasks/{taskId}` (Audit logs for a task)
