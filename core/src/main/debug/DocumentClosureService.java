package com.docflow.docflow_core.service;

import com.docflow.docflow_core.entity.DocParent;
import com.docflow.docflow_core.entity.enums.DocumentStatus;
import com.docflow.docflow_core.model.RuleEvaluationResponse;
import com.docflow.docflow_core.repository.DocParentRepository;
import com.docflow.docflow_core.rules.service.RuleEvaluatorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

/**
 * Service layer handling closure evaluation and dynamic rule lookups.
 */
@Service
public class DocumentClosureService {

    private final RuleEvaluatorRegistry registry;
    private final DocParentRepository docParentRepository;

    @Autowired
    public DocumentClosureService(RuleEvaluatorRegistry registry, DocParentRepository docParentRepository) {
        this.registry = registry;
        this.docParentRepository = docParentRepository;
    }

    /**
     * Evaluate rules dynamically based on the given rule type.
     *
     * @param parentId The document (parent) ID.
     * @param ruleType Type of rule (e.g., CLOSURE, EXPIRY, etc.)
     */
    public RuleEvaluationResponse evaluateRules(Long parentId, String ruleType) {
        var evaluator = registry.getEvaluator(ruleType);

        if (evaluator == null) {
            return new RuleEvaluationResponse(
                    parentId,
                    false,
                    java.util.List.of("No evaluator found for rule type: " + ruleType)
            );
        }

        return evaluator.evaluateRules(parentId);
    }

    public RuleEvaluationResponse validateDocumentClosure(Long parentId) {
        return evaluateRules(parentId, "CLOSURE");
    }

    @Transactional
    public boolean markDocumentAsCompleted(Long parentId) {
        Optional<DocParent> parentOpt = docParentRepository.findById(parentId);
        if (parentOpt.isEmpty()) {
            return false;
        }

        DocParent parent = parentOpt.get();
        parent.setStatus(DocumentStatus.COMPLETED);
        parent.setLastUpdated(Timestamp.from(Instant.now()).toLocalDateTime());
        docParentRepository.save(parent);
        return true;
    }

}
