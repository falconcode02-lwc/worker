package io.falconFlow.controller;

import io.falconFlow.entity.SecretEntity;
import io.falconFlow.services.isolateservices.PluginManagerService;
import io.falconFlow.services.secret.SecretDto;
import io.falconFlow.services.secret.SecretService;
import io.falconFlow.services.secret.SecretServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/secrets")
public class SecretController {

    private final SecretService secretService;
    private final SecretServiceImpl secretServiceImpl;

    private static final Set<String> ALLOWED_VAULT_TYPES = Set.of("DB", "AZURE", "GCP");

    @Autowired
    PluginManagerService pluginService;

    @Autowired
    public SecretController(SecretService secretService, SecretServiceImpl secretServiceImpl) {
        this.secretService = secretService;
        this.secretServiceImpl = secretServiceImpl;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SecretDto request) {
        String vaultType = (request == null) ? "DB" : request.getVaultTypeOrDefault();
        if (!ALLOWED_VAULT_TYPES.contains(vaultType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid vaultType. Allowed: DB, AZURE, GCP");
        }

        // Common success response across vaults.
        // - DB: reuse existing DB flow
        // - AZURE/GCP: bypass DB and store in external vault
        if ("DB".equals(vaultType)) {
            SecretEntity created = secretService.create(request.toEntity());
            return ResponseEntity.created(URI.create("/api/secrets/" + created.getId())).body(created);
        }

        secretServiceImpl.storeByVaultType(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SecretEntity> get(@PathVariable Long id) {
        return secretService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<SecretEntity>> list() {
        return ResponseEntity.ok(secretService.list());
    }

    @GetMapping("/getByType")
    public ResponseEntity<List<SecretEntity>> listByType(@RequestParam(defaultValue = "") String type, @RequestParam(defaultValue = "") String isDataKeys) {
        return ResponseEntity.ok(secretService.findByType(type, isDataKeys));
    }


    @PutMapping("/{id}")
    public ResponseEntity<SecretEntity> update(@PathVariable Long id, @RequestBody SecretEntity secret) {
        SecretEntity updated = secretService.update(id, secret);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        secretService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
