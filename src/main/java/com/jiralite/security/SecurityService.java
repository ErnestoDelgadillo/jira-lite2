package com.jiralite.security;

import com.jiralite.domain.Project;
import com.jiralite.domain.Role;
import com.jiralite.domain.User;
import com.jiralite.exception.AccessDeniedException;
import com.jiralite.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    private final UserRepository userRepository;

    public SecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String username;
        if (auth.getPrincipal() instanceof Jwt jwt) {
            username = jwt.getSubject();
        } else {
            username = auth.getName();
        }

        if (username == null || "anonymousUser".equalsIgnoreCase(username)) {
            throw new AccessDeniedException("User is not authenticated");
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    public void requireAdmin() {
        User user = getCurrentUser();
        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Admin role required for this action");
        }
    }

    public void requireManagerOrAdmin() {
        User user = getCurrentUser();
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.MANAGER) {
            throw new AccessDeniedException("Manager or Admin role required for this action");
        }
    }

    public void requireNotViewer() {
        User user = getCurrentUser();
        if (user.getRole() == Role.VIEWER) {
            throw new AccessDeniedException("Viewers have read-only access for this action");
        }
    }

    public void requireProjectMemberOrAdmin(Project project) {
        User user = getCurrentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (!isMember(project, user)) {
            throw new AccessDeniedException("User is not a member of project: " + project.getName());
        }
    }

    public void requireProjectManagerOrAdmin(Project project) {
        User user = getCurrentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.MANAGER && isMember(project, user)) {
            return;
        }
        throw new AccessDeniedException("Manager role on this project or Admin role required");
    }

    public boolean isMember(Project project, User user) {
        if (project == null || user == null || project.getMembers() == null) {
            return false;
        }
        return project.getMembers().stream()
                .anyMatch(m -> m.getId() != null && m.getId().equals(user.getId()));
    }

    public boolean isManagerOrAdmin(User user) {
        return user != null && (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER);
    }
}

