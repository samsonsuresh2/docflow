package com.docflow.docflow_core.rules.service;

import com.docflow.docflow_core.model.RuleEvaluationResponse;
import com.docflow.docflow_core.rules.annotation.RuleType;
import com.docflow.docflow_core.rules.repository.DocRuleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates all active closure rules for a given document.
 * 1. Loads active rules.
 * 2. Executes each rule's expression against the child table.
 * 3. Writes results into DOC_RULE_AUDIT.
 * 4. Returns overall canClose flag and failure reasons.
 */
@Service
@RuleType("CLOSURE")
public class ClosureEvaluatorService implements RuleEvaluationService{

    private static final Logger log=LoggerFactory.getLogger(ClosureEvaluatorService.class);
    private final DocRuleRepository ruleRepo;
    private final JdbcTemplate jdbcTemplate;

    public ClosureEvaluatorService(DocRuleRepository ruleRepo, JdbcTemplate jdbcTemplate) {
        this.ruleRepo = ruleRepo;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Evaluates all ACTIVE rules for the given parent document.
     * - Short-circuits on first failure if configured.
     * - Logs each rule result into DOC_RULE_AUDIT.
     * - Returns canClose + reasons in a simple map.
     */
    public RuleEvaluationResponse evaluateRules(Long parentId) {
        // Load only active rules
        java.util.List<com.docflow.docflow_core.rules.entity.DocRule> rules =
                ruleRepo.findByRuleContextAndActive("CLOSURE","Y");

        log.info("Evaluating {} active rules for context: {}", rules.size(), "CLOSURE");
        rules.forEach(r -> log.debug("Rule {} - {}", r.getRuleType(), r.getDescription()));

        java.util.List<String> failedReasons = new java.util.ArrayList<>();
        boolean canClose = true;

        for (com.docflow.docflow_core.rules.entity.DocRule rule : rules) {
            // Build NOT (...) wrapper so COUNT(*) > 0 means failures exist
            String sql = buildRuleQuery(rule.getExpression(), parentId);

            // Execute the rule: if any row violates the expression, count > 0
            Integer violatingCount = jdbcTemplate.queryForObject(sql, Integer.class);

            boolean passed = (violatingCount != null && violatingCount == 0);

            // Audit every evaluation (even if we short-circuit after this one)
            jdbcTemplate.update("""
            INSERT INTO DOC_RULE_AUDIT (DOC_ID, RULE_ID, RESULT, DETAILS)
            VALUES (?, ?, ?, ?)
        """, parentId,
                    rule.getRuleId(),
                    passed ? "PASS" : "FAIL",
                    passed ? null : ("Expression failed: " + rule.getExpression()));

            if (!passed) {
                canClose = false;
                // Use human-readable description if present, else fall back to type
                String msg = (rule.getDescription() != null && !rule.getDescription().isBlank())
                        ? "Rule failed: " + rule.getDescription()
                        : "Rule failed: " + rule.getRuleType();
                failedReasons.add(msg);

                // ⏩ Stop evaluating further rules if short-circuit is enabled
                if (shortCircuitEnabled) {
                    break;
                }
            }
        }

        List<String> reasons = List.of(); // add actual reasons as per DB checks
        return new RuleEvaluationResponse(parentId, canClose, failedReasons);
    }


    // Builds a dynamic rule query by wrapping expression inside NOT EXISTS
    private String buildRuleQuery(String expression, Long parentId) {
        return """
            SELECT COUNT(*) FROM DOC_CHILD
            WHERE PARENT_ID = %d AND NOT (%s)
        """.formatted(parentId, expression);
    }

    // Injecting if the can-close should short circuit and exit on first failure
    @Value("${docflow.rules.short-circuit:true}")
    private boolean shortCircuitEnabled;
}
