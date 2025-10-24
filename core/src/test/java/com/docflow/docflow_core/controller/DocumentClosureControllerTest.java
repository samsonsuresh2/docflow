package com.docflow.docflow_core.controller;

import com.docflow.docflow_core.model.RuleEvaluationResponse;
import com.docflow.docflow_core.service.DocumentClosureService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentClosureControllerTest {

    @Mock
    private DocumentClosureService documentClosureService;

    @InjectMocks
    private DocumentClosureController controller;

    @Test
    void testValidateClosureSuccess() {
        RuleEvaluationResponse response = new RuleEvaluationResponse(10L,true, List.of());
        when(documentClosureService.validateDocumentClosure(10L)).thenReturn(response);

        ResponseEntity<?> result = controller.markClosure(10L);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void testValidateClosureFail() {
        RuleEvaluationResponse response = new RuleEvaluationResponse(11L,false, List.of("Rule failed"));
        when(documentClosureService.validateDocumentClosure(11L)).thenReturn(response);

        ResponseEntity<?> result = controller.markClosure(11L);
        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
    }

    @Test
    void testMarkClosure() {
        when(documentClosureService.validateDocumentClosure(12L))
                .thenReturn(new RuleEvaluationResponse(12L,true, List.of()));
        when(documentClosureService.markDocumentAsCompleted(12L)).thenReturn(true);

        ResponseEntity<?> result = controller.markClosure(12L);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }
}
