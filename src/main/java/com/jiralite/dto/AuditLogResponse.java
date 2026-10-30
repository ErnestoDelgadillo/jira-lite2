package com.jiralite.dto;

import com.jiralite.domain.AuditEventType;
import com.jiralite.domain.AuditLog;

import java.time.Instant;

public class AuditLogResponse {
    private Long id;
    private AuditEventType eventType;
    private String actor;
    private String entityType;
    private Long entityId;
    private Long projectId;
    private Long taskId;
    private String details;
    private Instant timestamp;

    public AuditLogResponse() {
    }

    public AuditLogResponse(Long id, AuditEventType eventType, String actor, String entityType,
                            Long entityId, Long projectId, Long taskId, String details, Instant timestamp) {
        this.id = id;
        this.eventType = eventType;
        this.actor = actor;
        this.entityType = entityType;
        this.entityId = entityId;
        this.projectId = projectId;
        this.taskId = taskId;
        this.details = details;
        this.timestamp = timestamp;
    }

    public static AuditLogResponse fromEntity(AuditLog log) {
        if (log == null) {
            return null;
        }
        return new AuditLogResponse(
                log.getId(),
                log.getEventType(),
                log.getActor(),
                log.getEntityType(),
                log.getEntityId(),
                log.getProjectId(),
                log.getTaskId(),
                log.getDetails(),
                log.getTimestamp()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public void setEventType(AuditEventType eventType) {
        this.eventType = eventType;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
