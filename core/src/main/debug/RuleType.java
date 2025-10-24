package com.docflow.docflow_core.rules.annotation;

import java.lang.annotation.*;

/**
 * Marks a RuleEvaluationService implementation with its rule type name.
 * Example: @RuleType("CLOSURE") or @RuleType("EXPIRY")
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RuleType {
    String value();
}
