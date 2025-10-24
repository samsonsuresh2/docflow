package com.docflow.docflow_core.entity;

import com.docflow.docflow_core.entity.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing the top-level document record.
 * Each document may have multiple child entries (rows) for reconciliation.
 */
@Setter
@Getter
@Entity
@Table(name = "DOC_PARENT")
public class DocParent {

    // ---- Getters & Setters ----
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PARENT_ID")
    private Long parentId;

    @Column(name = "DOC_NAME", nullable = false, length = 255)
    private String docName;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 30, nullable = false)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @Column(name = "UPLOADED_BY", length = 100)
    private String uploadedBy;

    @Column(name = "UPLOADED_AT")
    private LocalDateTime uploadedAt;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "CLIENT_DOC_ID", length = 50)
    private String clientDocId;

    @Column(name = "SYSTEM_DOC_ID", length = 50)
    private String systemDocId;

    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

}
