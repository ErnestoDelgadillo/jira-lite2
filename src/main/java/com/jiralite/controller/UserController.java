package com.jiralite.controller;

import com.jiralite.dto.RoleUpdateRequest;
import com.jiralite.dto.UserResponse;
import com.jiralite.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateUserRolePut(@PathVariable("id") Long id,
                                                          @Valid @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(userService.changeUserRole(id, request.getRole()));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateUserRolePatch(@PathVariable("id") Long id,
                                                            @Valid @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(userService.changeUserRole(id, request.getRole()));
    }
}
