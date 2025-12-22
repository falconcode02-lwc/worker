package io.falconFlow.services.ai.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class HttpMCPClient implements MCPClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public HttpMCPClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String generate(String url, String model, String prompt, Map<String, Object> meta) throws Exception {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("MCP endpoint URL is required");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (meta != null) {
            Object auth = meta.get("authToken");
            if (auth != null) {
                headers.setBearerAuth(String.valueOf(auth));
            }

            Object rawHeaders = meta.get("headers");
            if (rawHeaders instanceof Map) {
                Map<?,?> hmap = (Map<?,?>) rawHeaders;
                for (Map.Entry<?,?> e : hmap.entrySet()) {
                    if (e.getKey() != null && e.getValue() != null) {
                        headers.add(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
                    }
                }
            }
        }

        Map<String,Object> body = new HashMap<>();
        if (model != null) body.put("model", model);
        body.put("input", prompt == null ? "" : prompt);
        if (meta != null && meta.get("parameters") instanceof Map) {
            body.put("parameters", meta.get("parameters"));
        }

        String payload = mapper.writeValueAsString(body);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("MCP call failed: " + resp.getStatusCodeValue() + " - " + resp.getBody());
        }

        return resp.getBody();
    }

    @Override
    public void stream(String url, String model, String prompt, Map<String, Object> meta, java.util.function.Consumer<String> onMessage) throws Exception {
        // If URL looks like a websocket endpoint, use WebSocket client. Otherwise, try HTTP SSE (not implemented)
        if (!StringUtils.hasText(url)) throw new IllegalArgumentException("url is required for streaming");
        if (url.startsWith("ws://") || url.startsWith("wss://")) {
            WebSocketMCPClient ws = new WebSocketMCPClient();
            ws.stream(url, model, prompt, meta, onMessage);
            return;
        }

        // Basic fallback: perform a normal HTTP call and send the result as a single message
        String result = generate(url, model, prompt, meta);
        onMessage.accept(result);
    }
}
