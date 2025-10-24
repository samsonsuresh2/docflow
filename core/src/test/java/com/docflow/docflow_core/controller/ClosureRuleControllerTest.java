package com.docflow.docflow_core.controller;

import com.docflow.docflow_core.model.RuleEvaluationResponse;
import com.docflow.docflow_core.rules.controller.ClosureRuleController;
import com.docflow.docflow_core.rules.service.ClosureEvaluatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClosureRuleControllerTest {

    @Mock
    private ClosureEvaluatorService closureEvaluatorService;

    @InjectMocks
    private ClosureRuleController controller;

    @Test
    void testCanCloseDocument() {
        RuleEvaluationResponse response = new RuleEvaluationResponse(5L,true, List.of());
        when(closureEvaluatorService.evaluateRules(5L)).thenReturn(response);

        RuleEvaluationResponse result = (RuleEvaluationResponse) controller.canClose(5L);
        assertTrue(result.isCanClose());
        verify(closureEvaluatorService).evaluateRules(5L);
    }
}

