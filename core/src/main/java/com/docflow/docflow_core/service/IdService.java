package com.docflow.docflow_core.service;

import org.springframework.stereotype.Service;
import java.text.DecimalFormat;
import java.time.Year;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

@Service
public class IdService {
    private static final Logger log = LoggerFactory.getLogger(IdService.class);
    private final AtomicInteger systemCounter = new AtomicInteger(1);
    private final AtomicInteger clientRefCounter = new AtomicInteger(1);
    private final DecimalFormat fmt = new DecimalFormat("000000");

    public String nextSystemId() {
        String id = "DOC-" + Year.now() + "-" + fmt.format(systemCounter.getAndIncrement());
        log.debug("Generated systemId={}", id);
        return id;
    }

    public String nextClientRefId(String clientId) {
        String id = clientId.toUpperCase() + "/" + Year.now().getValue()%100 + "/" + fmt.format(clientRefCounter.getAndIncrement());        log.debug("Generated clientRefId={}", id);
        return id;
    }
}
