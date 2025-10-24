package com.docflow.docflow_core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity that represents any workflow or approval request
 * initiated against a document (maker/checker style).
 * Maps to DOC_REQUEST table.
 */
@Entity
@Table(name = "DOC_REQUEST")
public class DocRequest {

    /** Primary key (auto-generated) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many requests can relate to a single document.
     * This defines the foreign key DOC_ID linking to DOC_METADATA(ID).
     * FetchType.LAZY ensures that document details are loaded only when accessed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DOC_ID")
    private DocMetadata document;

    /** The type of workflow request (APPROVAL, REUPLOAD, UPDATE). */
    @Column(name = "REQUEST_TYPE")
    private String requestType;

    /** User ID or name who made the request. */
    @Column(name = "REQUEST_BY")
    private String requestBy;

    /** Timestamp when the request was raised. */
    @Column(name = "REQUEST_AT")
    private LocalDateTime requestAt;

    /** Optional textual comments provided by the requester. */
    @Column(name = "COMMENTS")
    private String comments;

    // --------------------- Getters and Setters -----------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DocMetadata getDocument() { return document; }
    public void setDocument(DocMetadata document) { this.document = document; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public String getRequestBy() { return requestBy; }
    public void setRequestBy(String requestBy) { this.requestBy = requestBy; }

    public LocalDateTime getRequestAt() { return requestAt; }
    public void setRequestAt(LocalDateTime requestAt) { this.requestAt = requestAt; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
