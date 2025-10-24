package com.docflow.docflow_core.controller;

import com.docflow.docflow_core.entity.ClientConfig;
import com.docflow.docflow_core.repository.ClientConfigRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/client") @RequiredArgsConstructor
public class ClientController {
    private final ClientConfigRepository repo;
    @PersistenceContext
    private EntityManager entityManager;
    @GetMapping("/config")
    public ClientConfig getConfig(@RequestParam String clientId) {
        return repo.findById(clientId).orElseThrow(() -> new RuntimeException("No config found for clientId=" + clientId));
    }

    @GetMapping("/config/all")
    public List<ClientConfig> getAll() {
        return repo.findAll();
    }

    @GetMapping("/config/debug/{clientId}")
    public ResponseEntity<?> debug(@PathVariable String clientId) {
        var meta = entityManager.getMetamodel().entity(ClientConfig.class);
        var idAttr = meta.getId(String.class).getName();
        var result = repo.findById(clientId);

        return ResponseEntity.ok(Map.of(
                "idField", idAttr,
                "idValuePassed", clientId,
                "found?", result.isPresent(),
                "recordCount", repo.count(),
                "allIds", repo.findAll().stream().map(ClientConfig::getClientId).toList()
        ));
    }
}
