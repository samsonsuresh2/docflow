package com.docflow.docflow_core.rules.controller;

import com.docflow.docflow_core.model.RuleEvaluationResponse;
import com.docflow.docflow_core.rules.service.ClosureEvaluatorService;
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

    private final ClosureEvaluatorService evaluatorService;

    public RulesController(ClosureEvaluatorService evaluatorService) {
        this.evaluatorService = evaluatorService;
    }

    /**
     * Evaluates all active rules for the specified parent document.
     * Returns result JSON used by UI or workflow orchestration.
     */
    @GetMapping("/evaluate/{parentId}")
    public RuleEvaluationResponse evaluate(@PathVariable Long parentId) {
        return evaluatorService.evaluateRules(parentId);
    }

    @GetMapping("/evaluate")
    public RuleEvaluationResponse evaluateWithQueryParams(
            @RequestParam Long parentId,
            @RequestParam(required = false) String ruleType) {
        // Current evaluator ignores ruleType; we keep signature for future use
        return evaluatorService.evaluateRules(parentId);
    }
}
