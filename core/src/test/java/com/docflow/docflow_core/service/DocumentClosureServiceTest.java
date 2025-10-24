package com.docflow.docflow_core.service;

import com.docflow.docflow_core.entity.DocParent;
import com.docflow.docflow_core.entity.enums.DocumentStatus;
import com.docflow.docflow_core.model.RuleEvaluationResponse;
import com.docflow.docflow_core.repository.DocParentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.docflow.docflow_core.rules.service.RuleEvaluatorRegistry;
import com.docflow.docflow_core.rules.service.ClosureEvaluatorService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentClosureServiceTest {

    @Mock
    private RuleEvaluatorRegistry registry;
    @Mock
    private ClosureEvaluatorService closureEvaluatorService;
    @Mock
    private DocParentRepository parentRepo;

    @InjectMocks
    private DocumentClosureService service;

    @Test
    void testEvaluateRulesViaRegistry() {
        RuleEvaluationResponse expected = new RuleEvaluationResponse(100L,true, List.of());
        when(registry.getEvaluator("CLOSURE")).thenReturn(closureEvaluatorService);
        when(closureEvaluatorService.evaluateRules(100L)).thenReturn(expected);

        RuleEvaluationResponse actual = service.evaluateRules(100L, "CLOSURE");

        assertTrue(actual.isCanClose());
        verify(closureEvaluatorService).evaluateRules(100L);
    }

    @Test
    void testValidateDocumentClosureUsesClosureContext() {
        RuleEvaluationResponse expected = new RuleEvaluationResponse(55L, false, List.of("Child pending"));
        when(registry.getEvaluator("CLOSURE")).thenReturn(closureEvaluatorService);
        when(closureEvaluatorService.evaluateRules(55L)).thenReturn(expected);

        RuleEvaluationResponse result = service.validateDocumentClosure(55L);

        assertFalse(result.isCanClose());
        assertEquals("Child pending", result.getReasons().get(0));
    }

    @Test
    void testMarkDocumentAsCompleted() {
        DocParent parent = new DocParent();
        parent.setParentId(10L);
        parent.setStatus(DocumentStatus.REVIEW);

        when(parentRepo.findById(10L)).thenReturn(Optional.of(parent));
        when(parentRepo.save(any(DocParent.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = service.markDocumentAsCompleted(10L);

        assertTrue(result);
        assertEquals("COMPLETED", parent.getStatus());
    }
}

