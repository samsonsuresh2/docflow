package com.docflow.docflow_core.rules.repository;

import com.docflow.docflow_core.rules.entity.DocRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Exposes CRUD + finder methods for rules.
 * Spring Data JPA auto-generates implementations at runtime.
 */
public interface DocRuleRepository extends JpaRepository<DocRule, Long> {
    List<DocRule> findByActive(String active);

    List<DocRule> findByRuleContextAndActive(String ruleContext, String active);

}
