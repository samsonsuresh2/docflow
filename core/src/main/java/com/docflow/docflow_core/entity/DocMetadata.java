package com.docflow.docflow_core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity class that represents a single uploaded document's metadata.
 * Maps directly to the DOC_METADATA table in Oracle.
 *
 * Purpose:
 *  - Store document-level information such as name, type, and upload details.
 *  - Acts as the parent entity for DocRequest (via DOC_ID foreign key).
 */
@Entity  // Marks this class as a JPA-managed entity
@Table(name = "DOC_METADATA")  // Explicitly maps to the table DOC_METADATA
public class DocMetadata {

    /**
     * Primary key column mapped to ID (auto-generated).
     * The GenerationType.IDENTITY allows Oracle to use its IDENTITY feature.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Original or logical name of the document. */
    @Column(name = "DOC_NAME", nullable = false)
    private String docName;

    /** Type/category of document (e.g., POLICY, CONTRACT, INVOICE). */
    @Column(name = "DOC_TYPE")
    private String docType;

    /** User or system account that uploaded the document. */
    @Column(name = "UPLOADED_BY")
    private String uploadedBy;

    /** Timestamp when the document was uploaded. */
    @Column(name = "UPLOADED_AT")
    private LocalDateTime uploadedAt;

    /** Workflow status – PENDING, APPROVED, or REJECTED. */
    @Column(name = "STATUS")
    private String status;

    // --------------------- Accessor Methods -----------------------

    /** Gets the primary key ID. */
    public Long getId() { return id; }

    /** Sets the primary key ID (normally auto-generated). */
    public void setId(Long id) { this.id = id; }

    /** Gets the document name. */
    public String getDocName() { return docName; }

    /** Sets the document name. */
    public void setDocName(String docName) { this.docName = docName; }

    /** Gets the document type/category. */
    public String getDocType() { return docType; }

    /** Sets the document type/category. */
    public void setDocType(String docType) { this.docType = docType; }

    /** Gets the uploader's name or ID. */
    public String getUploadedBy() { return uploadedBy; }

    /** Sets the uploader's name or ID. */
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    /** Gets the upload timestamp. */
    public LocalDateTime getUploadedAt() { return uploadedAt; }

    /** Sets the upload timestamp. */
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    /** Gets the workflow status. */
    public String getStatus() { return status; }

    /** Sets the workflow status. */
    public void setStatus(String status) { this.status = status; }
}
