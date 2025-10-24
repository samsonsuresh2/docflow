package com.docflow.docflow_core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.docflow.docflow_core.entity.DocRequest;

/**
 * REPOSITORY: DocRequestRepository
 * Handles persistence of workflow requests.
 */
public interface DocRequestRepository extends JpaRepository<DocRequest, Long> {
}
