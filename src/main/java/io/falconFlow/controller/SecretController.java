package io.falconFlow.controller;

import io.falconFlow.entity.SecretEntity;
import io.falconFlow.services.secret.SecretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/secrets")
public class SecretController {

    private final SecretService secretService;

    @Autowired
    public SecretController(SecretService secretService) {
        this.secretService = secretService;
    }

    @PostMapping
    public ResponseEntity<SecretEntity> create(@RequestBody SecretEntity secret) {
        SecretEntity created = secretService.create(secret);
        return ResponseEntity.created(URI.create("/api/secrets/" + created.getId())).body(created);
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
    public ResponseEntity<List<SecretEntity>> listByType(@RequestParam(defaultValue = "") String type) {
        return ResponseEntity.ok(secretService.findByType(type));
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
