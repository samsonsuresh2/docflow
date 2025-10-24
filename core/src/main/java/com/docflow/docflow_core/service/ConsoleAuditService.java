package com.docflow.docflow_core.service;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConsoleAuditService implements AuditService {
    private static final Logger log = LoggerFactory.getLogger(ConsoleAuditService.class);
    @Override
    public void record(String action, String user, String systemId, String details) {
        log.info("[AUDIT] action={} user={} doc={} details={}", action, user, systemId, details);
    }
}
