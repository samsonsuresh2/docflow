package com.docflow.docflow_core.repository;

import com.docflow.docflow_core.rules.entity.DocRule;
import com.docflow.docflow_core.rules.repository.DocRuleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
class DocRuleRepositoryTest {

    @Autowired
    private DocRuleRepository repository;

    @Test
    void testFindByRuleContextAndActive() {
        DocRule rule = new DocRule();
        rule.setRuleContext("CLOSURE");
        rule.setActive("Y");
        repository.save(rule);

        List<DocRule> results = repository.findByRuleContextAndActive("CLOSURE", "Y");
        assertFalse(results.isEmpty());
    }
}
