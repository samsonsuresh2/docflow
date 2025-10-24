package com.docflow.docflow_core.controller;

import com.docflow.docflow_core.model.RuleEvaluationResponse;
import com.docflow.docflow_core.service.DocumentClosureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * REST controller to test dynamic rule evaluation.
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentClosureController {

    private final DocumentClosureService documentClosureService;

    @Autowired
    public DocumentClosureController(DocumentClosureService documentClosureService) {
        this.documentClosureService = documentClosureService;
    }

    /**
     * Example:
     *   GET /api/rules/evaluate?parentId=1&ruleType=CLOSURE
     */
    @GetMapping("/validate-closure")
    public RuleEvaluationResponse evaluateRule(
            @RequestParam Long parentId,
            @RequestParam(defaultValue = "CLOSURE") String ruleType) {

        return documentClosureService.evaluateRules(parentId, ruleType);
    }

    // ✅ NEW: Manual “mark as completed” – validates first, then updates status
    //@PostMapping("/{parentId}/mark-closure")
    //To-BE changed later
    @GetMapping("/{parentId}/mark-closure")
    public ResponseEntity<?> markClosure(@PathVariable Long parentId) {
        RuleEvaluationResponse result = documentClosureService.validateDocumentClosure(parentId);

        if (!result.isCanClose()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", false,
                            "message", "Document cannot be closed until all rules pass.",
                            "reasons", result.getReasons()
                    ));
        }

        boolean updated = documentClosureService.markDocumentAsCompleted(parentId);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Document not found."));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Document successfully marked as COMPLETED."
        ));
    }

}
