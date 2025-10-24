package com.docflow.docflow_core.rules.service;

import com.docflow.docflow_core.rules.annotation.RuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for all rule evaluators.
 * Uses @RuleType annotation for explicit mapping.
 */
@Component
public class RuleEvaluatorRegistry {

    private final Map<String, RuleEvaluationService> registry = new HashMap<>();

    @Autowired
    public RuleEvaluatorRegistry(Map<String, RuleEvaluationService> evaluators) {
        evaluators.forEach((beanName, service) -> {
            // Look for @RuleType annotation on each service
            RuleType ruleType = service.getClass().getAnnotation(RuleType.class);
            if (ruleType != null) {
                registry.put(ruleType.value().toUpperCase(), service);
                System.out.println("Registered evaluator: " + ruleType.value().toUpperCase());
            } else {
                System.out.println("⚠️ Skipped evaluator without @RuleType: " + beanName);
            }
        });
    }

    public RuleEvaluationService getEvaluator(String ruleType) {
        return registry.get(ruleType.toUpperCase());
    }

    public Map<String, RuleEvaluationService> getAll() {
        return registry;
    }
}
