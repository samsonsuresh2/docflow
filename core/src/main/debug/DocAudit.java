package com.docflow.docflow_core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity class mapping to DOC_AUDIT.
 * Used to track all actions performed in the application,
 * including user uploads, admin approvals, and metadata changes.
 */
@Entity
@Table(name = "DOC_AUDIT")
public class DocAudit {

    /** Unique audit record ID (auto-generated). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The action name (UPLOAD, APPROVE, DELETE, etc.). */
    @Column(name = "ACTION")
    private String action;

    /** The user who performed the action. */
    @Column(name = "USER_NAME")
    private String userName;

    /** Timestamp when the action occurred. */
    @Column(name = "ACTION_TIME")
    private LocalDateTime actionTime;

    /**
     * Free-form details or structured JSON log.
     * Stored as CLOB since details can be large.
     */
    @Lob
    @Column(name = "DETAILS")
    private String details;

    // --------------------- Getters and Setters -----------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
