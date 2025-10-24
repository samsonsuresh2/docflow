package com.docflow.docflow_core.rules.controller;

import com.docflow.docflow_core.model.RuleEvaluationResponse;
import com.docflow.docflow_core.service.DocumentClosureService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller exposing APIs for evaluating document closure rules.
 *
 * Typical usage:
 *   GET /api/rules/evaluate/{parentId}
 *
 * Response:
 *   {
 *     "docId": 1,
 *     "canClose": true,
 *     "reasons": []
 *   }
 */
@RestController
@RequestMapping("/api/rules")
public class RulesController {

    private final DocumentClosureService documentClosureService;

    public RulesController(DocumentClosureService documentClosureService) {
        this.documentClosureService = documentClosureService;
    }

    /**
     * Evaluates all active rules for the specified parent document.
     * Returns result JSON used by UI or workflow orchestration.
     */
    @GetMapping("/evaluate/{parentId}")
    public RuleEvaluationResponse evaluate(@PathVariable Long parentId) {
        return documentClosureService.validateDocumentClosure(parentId);
    }

    /*
     * GET /api/rules/evaluate?parentId=...&ruleType=...
     */
    @GetMapping("/evaluate")
    public RuleEvaluationResponse evaluateWithQueryParams(
            @RequestParam Long parentId,
            @RequestParam(required = false, defaultValue = "CLOSURE") String ruleType) {
        // now actually honor ruleType via the registry
        return documentClosureService.evaluateRules(parentId, ruleType);
    }
}
