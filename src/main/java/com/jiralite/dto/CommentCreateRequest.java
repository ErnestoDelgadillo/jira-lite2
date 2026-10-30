package com.jiralite.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentCreateRequest {

    @NotBlank(message = "Comment body cannot be blank")
    private String body;

    public CommentCreateRequest() {
    }

    public CommentCreateRequest(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

