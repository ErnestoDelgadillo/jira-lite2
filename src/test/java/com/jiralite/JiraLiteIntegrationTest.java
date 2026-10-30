package com.jiralite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiralite.domain.Role;
import com.jiralite.domain.TaskPriority;
import com.jiralite.domain.TaskStatus;
import com.jiralite.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class JiraLiteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String managerToken;
    private String developerToken;
    private String viewerToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = obtainToken("admin", "admin123");
        managerToken = obtainToken("manager", "manager123");
        developerToken = obtainToken("developer", "developer123");
        viewerToken = obtainToken("viewer", "viewer123");
    }

    private String obtainToken(String username, String password) throws Exception {
        AuthRequest req = new AuthRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    @Test
    @DisplayName("Auth: Register new user, validate duplicates, and login")
    void testRegisterAndLogin() throws Exception {
        RegisterRequest register = new RegisterRequest("alice", "alicepass", Role.DEVELOPER);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("alice")))
                .andExpect(jsonPath("$.role", is("DEVELOPER")));

        // Duplicate username rejected
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isBadRequest());

        // Login with new user
        AuthRequest login = new AuthRequest("alice", "alicepass");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.username", is("alice")));

        // Bad credentials
        AuthRequest badLogin = new AuthRequest("alice", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Users: List users and change role (admin only)")
    void testUserManagement() throws Exception {
        // Any authenticated user can list users
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))));

        // Non-admin cannot change role
        RoleUpdateRequest roleUpdate = new RoleUpdateRequest(Role.MANAGER);
        mockMvc.perform(put("/api/users/4/role")
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleUpdate)))
                .andExpect(status().isForbidden());

        // Admin can change role
        mockMvc.perform(put("/api/users/4/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("MANAGER")));

        // Change back to VIEWER
        mockMvc.perform(put("/api/users/4/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoleUpdateRequest(Role.VIEWER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("VIEWER")));
    }

    @Test
    @DisplayName("Public Access: Root / returns 200 OK without requiring authentication")
    void testRootEndpointPublicAccess() throws Exception {
        // As JSON
        mockMvc.perform(get("/")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.application", is("Jira Lite")));

        // As HTML (e.g. browser navigation)
        mockMvc.perform(get("/")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Security: Unauthenticated requests to protected endpoints are rejected with 401")
    void testUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Validation: Invalid input triggers 400 Bad Request")
    void testValidationFailures() throws Exception {
        // Blank username/password on login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest("", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors", notNullValue()));

        // Blank project name
        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProjectCreateRequest("", "desc"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Not Found: Non-existent entities return 404")
    void testNotFoundResponses() throws Exception {
        mockMvc.perform(get("/api/projects/9999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/tasks/9999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/sprints/9999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("End-to-End: Project, Sprint, Task, Workflow, Comments, and Audit Log")
    void testEndToEndWorkflow() throws Exception {
        // 1. Create Project by Manager
        ProjectCreateRequest projectReq = new ProjectCreateRequest("Project E2E", "End to end testing project");
        MvcResult projResult = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Project E2E")))
                .andReturn();
        Long projectId = objectMapper.readTree(projResult.getResponse().getContentAsString()).get("id").asLong();

        // Developer cannot create project
        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProjectCreateRequest("Dev Proj", "desc"))))
                .andExpect(status().isForbidden());

        // 2. Add Developer (user id 3) and Viewer (user id 4) to Project
        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddMemberRequest(3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members", hasSize(2)));

        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddMemberRequest(4L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members", hasSize(3)));

        // Duplicate member addition rejected
        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddMemberRequest(4L))))
                .andExpect(status().isBadRequest());

        // 3. Create Sprint by Manager
        SprintCreateRequest sprintReq = new SprintCreateRequest(
                projectId,
                "Sprint 1",
                LocalDate.now(),
                LocalDate.now().plusWeeks(2)
        );
        MvcResult sprintResult = mockMvc.perform(post("/api/sprints")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sprintReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PLANNED")))
                .andReturn();
        Long sprintId = objectMapper.readTree(sprintResult.getResponse().getContentAsString()).get("id").asLong();

        // Cannot complete sprint from PLANNED
        mockMvc.perform(post("/api/sprints/" + sprintId + "/complete")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest());

        // Start Sprint
        mockMvc.perform(post("/api/sprints/" + sprintId + "/start")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        // Cannot start an already ACTIVE sprint
        mockMvc.perform(post("/api/sprints/" + sprintId + "/start")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest());

        // 4. Create Task by Developer
        TaskCreateRequest taskReq = new TaskCreateRequest(
                projectId,
                "Implement Authentication Service",
                "Use Spring Security + JWT",
                TaskPriority.HIGH,
                3L,
                sprintId,
                LocalDate.now().plusDays(5)
        );
        MvcResult taskResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.priority", is("HIGH")))
                .andReturn();
        Long taskId = objectMapper.readTree(taskResult.getResponse().getContentAsString()).get("id").asLong();

        // Viewer cannot create task
        TaskCreateRequest viewerTask = new TaskCreateRequest(projectId, "Viewer Task", "desc", TaskPriority.LOW, null, null, null);
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(viewerTask)))
                .andExpect(status().isForbidden());

        // 5. Workflow Transitions:
        // OPEN -> IN_PROGRESS (allowed for Developer)
        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskTransitionRequest(TaskStatus.IN_PROGRESS))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        // IN_PROGRESS -> CLOSED (invalid jump! must reject with 400)
        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskTransitionRequest(TaskStatus.CLOSED))))
                .andExpect(status().isBadRequest());

        // IN_PROGRESS -> IN_REVIEW (allowed for Developer)
        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskTransitionRequest(TaskStatus.IN_REVIEW))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_REVIEW")));

        // IN_REVIEW -> DONE (allowed for Developer)
        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskTransitionRequest(TaskStatus.DONE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DONE")));

        // DONE -> CLOSED by Developer (Forbidden! Only Manager or Admin can close)
        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskTransitionRequest(TaskStatus.CLOSED))))
                .andExpect(status().isForbidden());

        // DONE -> CLOSED by Manager (Allowed!)
        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskTransitionRequest(TaskStatus.CLOSED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CLOSED")));

        // CLOSED -> OPEN by Developer (Forbidden! Only Manager or Admin can reopen)
        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskTransitionRequest(TaskStatus.OPEN))))
                .andExpect(status().isForbidden());

        // CLOSED -> OPEN by Admin (Allowed!)
        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskTransitionRequest(TaskStatus.OPEN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OPEN")));

        // 6. Comments:
        // Any member, including Viewer, can add comments
        CommentCreateRequest commentReq = new CommentCreateRequest("Testing comment from reviewer");
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body", is("Testing comment from reviewer")))
                .andExpect(jsonPath("$.author.username", is("viewer")));

        // List comments
        mockMvc.perform(get("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].body", is("Testing comment from reviewer")));

        // 7. Complete Sprint
        mockMvc.perform(post("/api/sprints/" + sprintId + "/complete")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        // 8. Task Filtering
        mockMvc.perform(get("/api/tasks?projectId=" + projectId + "&status=OPEN&priority=HIGH")
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(taskId.intValue())));

        mockMvc.perform(get("/api/tasks?projectId=" + projectId + "&status=CLOSED")
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // 9. Audit Log: per-project and per-task readouts
        mockMvc.perform(get("/api/audit-logs/projects/" + projectId)
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$[0].projectId", is(projectId.intValue())));

        mockMvc.perform(get("/api/audit-logs/tasks/" + taskId)
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$[0].taskId", is(taskId.intValue())));
    }
}
