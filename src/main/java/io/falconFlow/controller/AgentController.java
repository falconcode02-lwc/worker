package io.falconFlow.controller;

import io.falconFlow.model.ai.AgentRequest;
import io.falconFlow.model.ai.AgentResponse;
import io.falconFlow.services.ai.AIService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import io.falconFlow.model.ai.AgentSummary;

@RestController
@RequestMapping("/api/ai")
public class AgentController {
    @Autowired
    private AIService aiService;

    @GetMapping("/agents")
    public ResponseEntity<List<AgentSummary>> listAgents() {
        return ResponseEntity.ok(aiService.listAgents());
    }

    @PostMapping("/agents")
    public ResponseEntity<?> createAgent(@RequestBody AgentRequest req) {
        aiService.createAgent(req.getAgentId(), req.getModel(), req.getConfig());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/agents/{agentId}/message")
    public ResponseEntity<AgentResponse> message(@PathVariable String agentId, @RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "");
        AgentResponse resp = aiService.sendMessage(agentId, message);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/agents/{agentId}/memory")
    public ResponseEntity<Object> memory(@PathVariable String agentId) {
        return ResponseEntity.ok(aiService.getMemory(agentId));
    }

    @PostMapping("/agents/{agentId}/tools/n8n")
    public ResponseEntity<Object> callN8n(@PathVariable String agentId, @RequestBody Map<String, Object> body) {
        // n8n integration removed — providers should implement any external actions
        return ResponseEntity.status(410).body("n8n integration removed; use provider-driven actions");
    }
}

 