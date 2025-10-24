package com.docflow.docflow_core.model;

import java.util.List;

/**
 * POJO representing the outcome of rule evaluation.
 * Automatically serialized to JSON by Spring Boot.
 */
public class RuleEvaluationResponse {

    private Long docId;
    private boolean canClose;
    private List<String> reasons;

    public RuleEvaluationResponse(Long docId, boolean canClose, List<String> reasons) {
        this.docId = docId;
        this.canClose = canClose;
        this.reasons = reasons;
    }

    public Long getDocId() {
        return docId;
    }

    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public boolean isCanClose() {
        return canClose;
    }

    public void setCanClose(boolean canClose) {
        this.canClose = canClose;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }
}
