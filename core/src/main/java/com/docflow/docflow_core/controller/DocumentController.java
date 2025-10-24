package com.docflow.docflow_core.controller;

import com.docflow.docflow_core.entity.DocParent;
import com.docflow.docflow_core.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api/documents") @RequiredArgsConstructor
public class DocumentController {
    private final UploadService uploadService;

    @PostMapping("/upload")
    public DocParent upload(@RequestParam("file") MultipartFile file,
                            @RequestParam("clientId") String clientId,
                            @RequestParam("username") String username) throws Exception {
        return uploadService.upload(file, clientId, username);
    }
}
