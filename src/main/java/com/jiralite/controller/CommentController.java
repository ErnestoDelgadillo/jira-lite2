package com.jiralite.controller;

import com.jiralite.dto.CommentCreateRequest;
import com.jiralite.dto.CommentResponse;
import com.jiralite.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@PathVariable("taskId") Long taskId,
                                                      @Valid @RequestBody CommentCreateRequest request) {
        CommentResponse response = commentService.addComment(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable("taskId") Long taskId) {
        return ResponseEntity.ok(commentService.getComments(taskId));
    }
}
