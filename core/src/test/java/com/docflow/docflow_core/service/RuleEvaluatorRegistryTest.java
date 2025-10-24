package com.docflow.docflow_core.service;


import com.docflow.docflow_core.rules.annotation.RuleType;
import com.docflow.docflow_core.rules.service.ClosureEvaluatorService;

import com.docflow.docflow_core.rules.service.RuleEvaluatorRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class RuleEvaluatorRegistryTest {

    @Mock
    private ApplicationContext context;

    @InjectMocks
    private RuleEvaluatorRegistry registry;

    @Test
    void testGetEvaluatorWhenPresent() {
        ClosureEvaluatorService closure = new ClosureEvaluatorService(null, null);
        when(context.getBeansWithAnnotation(RuleType.class))
                .thenReturn(Map.of("closureEvaluatorService", closure));


        Object evaluator = registry.getEvaluator("CLOSURE");
        assertNotNull(evaluator);
        assertInstanceOf(ClosureEvaluatorService.class, evaluator);
    }

    @Test
    void testGetEvaluatorThrowsForMissing() {
        when(context.getBeansWithAnnotation(RuleType.class)).thenReturn(Map.of());

        assertThrows(IllegalArgumentException.class, () -> registry.getEvaluator("UNKNOWN"));
    }
}
