package com.docflow.docflow_core.controller;

import com.docflow.docflow_core.model.RuleEvaluationResponse;
import com.docflow.docflow_core.rules.controller.RulesController;
import com.docflow.docflow_core.service.DocumentClosureService;
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
class RulesControllerTest {

    @Mock
    private DocumentClosureService documentClosureService;

    @InjectMocks
    private RulesController controller;

    @Test
    void testEvaluateRulesByPath() {
        RuleEvaluationResponse response = new RuleEvaluationResponse(1L,true, List.of());
        when(documentClosureService.validateDocumentClosure(1L)).thenReturn(response);

        RuleEvaluationResponse result = controller.evaluate(1L);
        assertTrue(result.isCanClose());
        verify(documentClosureService).validateDocumentClosure(1L);
    }

    @Test
    void testEvaluateRulesByQueryParam() {
        RuleEvaluationResponse response = new RuleEvaluationResponse(2L,false, List.of("Some child pending"));
        when(documentClosureService.evaluateRules(2L, "CLOSURE")).thenReturn(response);

        RuleEvaluationResponse result = controller.evaluateWithQueryParams(2L, "CLOSURE");
        assertFalse(result.isCanClose());
        assertEquals("Some child pending", result.getReasons().get(0));
        verify(documentClosureService).evaluateRules(2L, "CLOSURE");
    }
}
