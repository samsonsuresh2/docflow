package com.docflow.docflow_core.rules.controller;

import com.docflow.docflow_core.rules.service.ClosureEvaluatorService;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoint for closure eligibility check.
 * Example: GET /api/documents/101/can-close
 */
@RestController
@RequestMapping("/api/documents")
public class ClosureRuleController {

    private final ClosureEvaluatorService evaluator;

    public ClosureRuleController(ClosureEvaluatorService evaluator) {
        this.evaluator = evaluator;
    }

    @GetMapping("/{docId}/can-close")
    public Object canClose(@PathVariable Long docId) {
        return evaluator.evaluateRules(docId);
    }
}
