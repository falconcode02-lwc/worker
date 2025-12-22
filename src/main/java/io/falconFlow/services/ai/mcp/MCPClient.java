package io.falconFlow.services.ai.mcp;

import java.util.Map;
import java.util.function.Consumer;

public interface MCPClient {
    /**
     * Synchronously generate a response from an MCP endpoint.
     *
     * @param url full endpoint URL
     * @param model model name
     * @param prompt user prompt
     * @param meta optional metadata (headers, authToken, parameters)
     * @return raw response body (string)
     */
    String generate(String url, String model, String prompt, Map<String, Object> meta) throws Exception;

    /**
     * Stream responses from an MCP endpoint. Implementations may use WebSocket/SSE.
     * The consumer will be called for each chunk or message received.
     */
    default void stream(String url, String model, String prompt, Map<String, Object> meta, Consumer<String> onMessage) throws Exception {
        // default fallback: single-shot generate then emit
        String r = generate(url, model, prompt, meta);
        onMessage.accept(r);
    }
}
