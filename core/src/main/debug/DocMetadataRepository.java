package com.docflow.docflow_core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.docflow.docflow_core.entity.DocMetadata;

/**
 * REPOSITORY: DocMetadataRepository
 * -------------------------------------------------------------------
 * Exposes CRUD operations for DocMetadata entity.
 * Spring Data JPA automatically implements methods like:
 *   - findAll(), findById(), save(), deleteById()
 * -------------------------------------------------------------------
 */
public interface DocMetadataRepository extends JpaRepository<DocMetadata, Long> {
}
