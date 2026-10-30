package com.jiralite.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class HomeController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("application", "Jira Lite");
        info.put("status", "UP");
        info.put("description", "Jira Lite task management backend");
        info.put("auth", Map.of(
                "type", "JWT (Bearer)",
                "loginEndpoint", "/api/auth/login",
                "registerEndpoint", "/api/auth/register"
        ));
        info.put("preconfiguredUsers", Map.of(
                "admin", "admin123 (ADMIN)",
                "manager", "manager123 (MANAGER)",
                "developer", "developer123 (DEVELOPER)",
                "viewer", "viewer123 (VIEWER)"
        ));
        info.put("endpoints", Map.of(
                "users", "/api/users",
                "projects", "/api/projects",
                "sprints", "/api/sprints",
                "tasks", "/api/tasks",
                "auditLogs", "/api/audit-logs",
                "h2Console", "/h2-console"
        ));
        return ResponseEntity.ok(info);
    }
}
