package com.docflow.docflow_core.service;

import com.docflow.docflow_core.entity.DocParent;
import com.docflow.docflow_core.repository.DocParentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.docflow.docflow_core.entity.enums.DocumentStatus;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@Service @RequiredArgsConstructor
public class UploadService {
    private static final Logger log = LoggerFactory.getLogger(UploadService.class);
    private final IdService idService;
    private final AuditService auditService;
    private final DocParentRepository repo;
    @Value("${docflow.upload.base-path:uploads}")
    private String basePath;

    public DocParent upload(MultipartFile file, String clientId, String username) throws IOException {
        log.trace("Entering upload() clientId={} file={}", clientId, file.getOriginalFilename());
        String systemId = idService.nextSystemId();
        String clientRefId = idService.nextClientRefId(clientId);

        File dir = new File(basePath + clientId + "/" + systemId);
        if (!dir.exists() && !dir.mkdirs())
            throw new IOException("Cannot create directory " + dir.getAbsolutePath());
        File dest = new File(dir, file.getOriginalFilename());
        file.transferTo(dest);
        log.debug("Saved file to {}", dest.getAbsolutePath());

        DocParent doc = new DocParent();
        doc.setSystemDocId(systemId);
        doc.setClientDocId(clientRefId);
        doc.setDocName(file.getOriginalFilename());
        doc.setMimeType(file.getContentType());
        doc.setFileSize(file.getSize());
        doc.setStoragePath(dest.getAbsolutePath());
        doc.setUploadedBy(username);
        doc.setUploadedAt(LocalDateTime.now());
        doc.setStatus(DocumentStatus.OPEN);
        repo.save(doc);

        log.info("Upload completed systemId={} by user={}", systemId, username);
        auditService.record("UPLOAD", username, systemId, "Uploaded " + file.getOriginalFilename());
        return doc;
    }
}
