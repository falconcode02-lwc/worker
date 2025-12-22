package io.falconFlow.controller;

import io.falconFlow.entity.ProviderConfigEntity;
import io.falconFlow.repository.ProviderConfigRepository;
import io.falconFlow.services.ai.provider.ProviderConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// validation handled manually (no jakarta/javax validation on classpath)
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/provider-config")
public class ProviderConfigController {

    private final ProviderConfigRepository repo;
    private final ProviderConfigService service;

    @Autowired
    public ProviderConfigController(ProviderConfigRepository repo, ProviderConfigService service) {
        this.repo = repo;
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProviderConfigEntity cfg) {
        // prevent duplicate names
        if (cfg.getName() == null || cfg.getName().isBlank()) {
            return ResponseEntity.badRequest().body("provider name is required");
        }
        if (cfg.getApiUrl() == null || cfg.getApiUrl().isBlank()) {
            return ResponseEntity.badRequest().body("apiUrl is required");
        }
        Optional<ProviderConfigEntity> exists = repo.findByName(cfg.getName());
        if (exists.isPresent()) {
            return ResponseEntity.status(409).body("provider config with this name already exists");
        }
        ProviderConfigEntity created = repo.save(cfg);
        return ResponseEntity.created(URI.create("/api/provider-config/" + created.getName())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ProviderConfigEntity>> list() {
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{name}")
    public ResponseEntity<ProviderConfigEntity> getByName(@PathVariable String name) {
        Optional<ProviderConfigEntity> opt = repo.findByName(name);
        return opt.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{name}")
    public ResponseEntity<?> update(@PathVariable String name, @RequestBody ProviderConfigEntity update) {
        Optional<ProviderConfigEntity> opt = repo.findByName(name);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        ProviderConfigEntity existing = opt.get();

        if (update.getApiUrl() != null) {
            if (update.getApiUrl().isBlank()) return ResponseEntity.badRequest().body("apiUrl cannot be empty");
            existing.setApiUrl(update.getApiUrl());
        }
        if (update.getDefaultModel() != null) existing.setDefaultModel(update.getDefaultModel());
        if (update.getProperties() != null) existing.setProperties(update.getProperties());
        ProviderConfigEntity saved = repo.save(existing);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> delete(@PathVariable String name) {
        Optional<ProviderConfigEntity> opt = repo.findByName(name);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        repo.delete(opt.get());
        return ResponseEntity.noContent().build();
    }
}
