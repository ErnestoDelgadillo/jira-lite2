package com.jiralite.dto;

import com.jiralite.domain.Comment;
import java.time.Instant;

public class CommentResponse {
    private Long id;
    private Long taskId;
    private UserResponse author;
    private String body;
    private Instant createdAt;

    public CommentResponse() {
    }

    public CommentResponse(Long id, Long taskId, UserResponse author, String body, Instant createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.author = author;
        this.body = body;
        this.createdAt = createdAt;
    }

    public static CommentResponse fromEntity(Comment comment) {
        if (comment == null) {
            return null;
        }
        Long tId = comment.getTask() != null ? comment.getTask().getId() : null;
        return new CommentResponse(
                comment.getId(),
                tId,
                UserResponse.fromEntity(comment.getAuthor()),
                comment.getBody(),
                comment.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public UserResponse getAuthor() {
        return author;
    }

    public void setAuthor(UserResponse author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

