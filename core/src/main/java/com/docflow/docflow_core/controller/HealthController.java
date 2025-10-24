package com.docflow.docflow_core.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.docflow.docflow_core.repository.DocMetadataRepository;
import com.docflow.docflow_core.entity.DocMetadata;

/**
 * CONTROLLER: HealthController
 * -------------------------------------------------------------------
 * Purpose:
 *   Provides a simple endpoint to verify that:
 *     - Spring Boot application starts correctly
 *     - Oracle DB connection works fine
 *     - JPA mapping (Entity <-> Table) is successful
 *
 * Endpoint:
 *   GET /api/health/db
 *
 * Expected Output:
 *   Returns all rows from DOC_METADATA as JSON.
 *   (Even one record means full end-to-end success)
 * -------------------------------------------------------------------
 */
@RestController                    // Marks this class as a REST controller
@RequestMapping("/api/health")      // Base path for all health endpoints
public class HealthController {

    // ----------------------------------------------------------------
    // Inject the repository to access DOC_METADATA table.
    // Spring automatically creates the instance.
    // ----------------------------------------------------------------
    private final DocMetadataRepository metadataRepo;

    // Constructor-based dependency injection (recommended in Spring Boot)
    public HealthController(DocMetadataRepository metadataRepo) {
        this.metadataRepo = metadataRepo;
    }

    /**
     * API: GET /api/health/db
     * ----------------------------------------------------------------
     * Description:
     *   Fetches all rows from the DOC_METADATA table.
     *   This is used for DB connectivity testing only.
     *
     * Example Response:
     * [
     *   {
     *     "id": 1,
     *     "docName": "Sample Template",
     *     "docType": "POLICY",
     *     "uploadedBy": "system",
     *     "uploadedAt": "2025-10-23T...",
     *     "status": "APPROVED"
     *   }
     * ]
     * ----------------------------------------------------------------
     */
    @GetMapping("/db")
    public List<DocMetadata> testDbConnection() {
        // findAll() = SELECT * FROM DOC_METADATA
        return metadataRepo.findAll();
    }
}
