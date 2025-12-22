package io.falconFlow.controller;

import io.falconFlow.entity.PluginEntity;
import io.falconFlow.repository.PluginRepository;
import io.falconFlow.services.isolateservices.PluginDto;
import io.falconFlow.services.isolateservices.PluginManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plugins")
public class PluginController {

    private final PluginRepository pluginRepository;
    private final PluginManagerService pluginManagerService;

    @Autowired
    public PluginController(PluginRepository pluginRepository, PluginManagerService pluginManagerService) {
        this.pluginRepository = pluginRepository;
        this.pluginManagerService = pluginManagerService;
    }

    /**
     * List plugins with lazy loading (pagination) and optional search query `q` which
     * matches pluginName or pluginId (case-insensitive).
     *
     * Example: GET /api/plugins?q=auth&page=0&size=20
     */
    @GetMapping
    public Page<PluginDto> list(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<PluginEntity> ents;
        if (q == null || q.isBlank()) {
            ents = pluginRepository.findAll(pageable);
        } else {
            ents = pluginRepository.findByPluginNameContainingIgnoreCaseOrPluginIdContainingIgnoreCase(q, q, pageable);
        }
        return ents.map(PluginDto::fromEntity);
    }

    /**
     * Get a single plugin by database id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PluginDto> get(@PathVariable("id") Integer id) {
        return pluginManagerService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
