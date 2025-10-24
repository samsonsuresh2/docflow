package com.docflow.docflow_core.rules.service;

import com.docflow.docflow_core.model.RuleEvaluationResponse;

/**
 * Generic contract for evaluating document rules.
 * Different implementations can support various rule types.
 */
public interface RuleEvaluationService {

    /**
     * Evaluate applicable rules for a given parent (document) ID.
     *
     * @param parentId The document ID.
     * @return Structured rule evaluation result.
     */
    RuleEvaluationResponse evaluateRules(Long parentId);
}
