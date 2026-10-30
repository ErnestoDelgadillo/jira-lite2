package com.jiralite.dto;

import com.jiralite.domain.Role;
import com.jiralite.domain.User;

public class UserResponse {
    private Long id;
    private String username;
    private Role role;

    public UserResponse() {
    }

    public UserResponse(Long id, String username, Role role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
