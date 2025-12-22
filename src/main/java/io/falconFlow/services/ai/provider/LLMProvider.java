package io.falconFlow.services.ai.provider;

import java.util.Map;

public interface LLMProvider {
    /**
     * Generate a response using the provider.
     *
     * @param agentId agent identifier
     * @param model model name to use
     * @param prompt prompt or message to send
     * @param config per-agent config map (may contain secret name etc.)
     * @return provider response text
     */
    String generate(String agentId, String model, String prompt, Map<String, String> config) throws Exception;
}
