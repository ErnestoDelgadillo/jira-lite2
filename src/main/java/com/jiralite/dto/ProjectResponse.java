package com.jiralite.dto;

import com.jiralite.domain.Project;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private Set<UserResponse> members = new HashSet<>();

    public ProjectResponse() {
    }

    public ProjectResponse(Long id, String name, String description, Set<UserResponse> members) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.members = members != null ? members : new HashSet<>();
    }

    public static ProjectResponse fromEntity(Project project) {
        if (project == null) {
            return null;
        }
        Set<UserResponse> memberResponses = project.getMembers() == null ? new HashSet<>() :
                project.getMembers().stream()
                        .map(UserResponse::fromEntity)
                        .collect(Collectors.toSet());
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), memberResponses);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<UserResponse> getMembers() {
        return members;
    }

    public void setMembers(Set<UserResponse> members) {
        this.members = members;
    }
}

