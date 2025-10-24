package com.docflow.docflow_core.rules.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a single configurable rule used to determine
 * whether a parent document can transition to the "Completed" state.
 *
 * Each rule has:
 *  - type: defines logic family (CHILD_STATUS / DATE_CHECK)
 *  - targetEntity: usually DOC_CHILD
 *  - expression: SQL-like string evaluated dynamically
 */
@Entity
@Table(name = "DOC_RULES")
public class DocRule {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ruleId;

    @Setter
    @Getter
    @Column(name = "RULE_TYPE")
    private String ruleType; // e.g., STATUS_CHECK or DATE_CHECK

    @Setter
    @Getter
    @Column(name = "RULE_CONTEXT")
    private String ruleContext;

    @Setter
    @Getter
    private String targetEntity;
    @Setter
    @Getter
    private String expression;
    @Setter
    @Getter
    private String description;
    @Setter
    @Getter
    private String active;
    @Setter
    @Getter
    private LocalDateTime createdAt;


}
