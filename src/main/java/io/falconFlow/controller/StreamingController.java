package io.falconFlow.controller;

import io.falconFlow.services.ai.AIService;
import io.falconFlow.services.ai.provider.LLMProvider;
import io.falconFlow.services.ai.provider.StreamableLLMProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Controller to expose streaming LLM responses via Server-Sent Events (SSE).
 * POST /api/ai/agents/{agentId}/stream  -> body: { "message": "..." }
 */
@RestController
@RequestMapping("/api/ai")
public class StreamingController {

    private final AIService aiService;
    private final Map<String, LLMProvider> providers;
    private final ExecutorService exec = Executors.newCachedThreadPool();

    @Autowired
    public StreamingController(AIService aiService, @Autowired(required = false) Map<String, LLMProvider> providers) {
        this.aiService = aiService;
        this.providers = providers == null ? Map.of() : providers;
    }

    @PostMapping("/agents/{agentId}/stream")
    public ResponseEntity<?> stream(@PathVariable String agentId, @RequestBody Map<String, String> body) {
        String message = body == null ? null : body.get("message");
        if (message == null || message.isEmpty()) {
            return ResponseEntity.badRequest().body("missing 'message' in body");
        }

        Map<String, String> cfg = aiService.getAgentConfig(agentId);
        if (cfg == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("agent config not found");
        }

        // select provider similarly to AIServiceImpl: prefer config.provider, then chatgpt/openai, else first available
        String providerName = cfg.get("provider");
        LLMProvider selected = null;
        if (providerName != null) {
            if (providers.containsKey(providerName)) selected = providers.get(providerName);
            else if (providers.containsKey(providerName.toLowerCase())) selected = providers.get(providerName.toLowerCase());
        }
        if (selected == null) {
            if (providers.containsKey("chatgpt")) selected = providers.get("chatgpt");
            else if (providers.containsKey("openai")) selected = providers.get("openai");
            else if (!providers.isEmpty()) selected = providers.values().iterator().next();
        }

        if (selected == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("no LLM providers available");
        }

        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(10));

        // If provider supports streaming, delegate; otherwise fall back to single-shot
        if (selected instanceof StreamableLLMProvider) {
            StreamableLLMProvider sp = (StreamableLLMProvider) selected;
            exec.submit(() -> {
                try {
                    sp.stream(agentId, null, message, cfg, chunk -> {
                        try {
                            emitter.send(SseEmitter.event().data(chunk));
                        } catch (Exception e) {
                            // stop on send error
                            try { emitter.completeWithError(e); } catch (Exception ignored) {}
                        }
                    });
                    emitter.complete();
                } catch (Exception e) {
                    try { emitter.completeWithError(e); } catch (Exception ignored) {}
                }
            });
            return ResponseEntity.ok().body(emitter);
        } else {
            // non-streaming provider: call AIService.sendMessage and send single SSE event
            exec.submit(() -> {
                try {
                    var resp = aiService.sendMessage(agentId, message);
                    emitter.send(SseEmitter.event().data(resp == null ? "" : resp.getMessage()));
                    emitter.complete();
                } catch (Exception e) {
                    try { emitter.completeWithError(e); } catch (Exception ignored) {}
                }
            });
            return ResponseEntity.ok().body(emitter);
        }
    }
}
