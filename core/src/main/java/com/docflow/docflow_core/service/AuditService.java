package com.docflow.docflow_core.service;

public interface AuditService {
    void record(String action, String user, String systemId, String details);
}
