package com.docflow.docflow_core.service;

import com.docflow.docflow_core.model.RuleEvaluationResponse;
import com.docflow.docflow_core.rules.entity.DocRule;
import com.docflow.docflow_core.rules.repository.DocRuleRepository;
import com.docflow.docflow_core.rules.service.ClosureEvaluatorService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClosureEvaluatorServiceTest {

    @Mock
    private DocRuleRepository ruleRepo;
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ClosureEvaluatorService service;

    @Test
    void testEvaluateRulesWhenAllPass() {
        DocRule rule = new DocRule();
        rule.setExpression("SELECT COUNT(1) FROM CHILD WHERE STATUS!='APPROVED' AND PARENT_ID=:parentId");
        when(ruleRepo.findByRuleContextAndActive("CLOSURE", "Y"))
                .thenReturn(List.of(rule));
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(Integer.class)))
                .thenReturn(Integer.valueOf(0));

        RuleEvaluationResponse result = service.evaluateRules(1L);
        assertTrue(result.isCanClose());
    }

    @Test
    void testEvaluateRulesFailingRule() {
        DocRule rule = new DocRule();
        rule.setExpression("SELECT COUNT(1) FROM CHILD WHERE STATUS!='APPROVED' AND PARENT_ID=:parentId");
        when(ruleRepo.findByRuleContextAndActive("CLOSURE", "Y"))
                .thenReturn(List.of(rule));
        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(Integer.class)))
                .thenReturn(Integer.valueOf(3));


        RuleEvaluationResponse result = service.evaluateRules(2L);
        assertFalse(result.isCanClose());
        assertFalse(result.getReasons().isEmpty());
    }
}

