package io.falconFlow.services.ai.provider;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Optional interface for providers that support streaming output.
 */
public interface StreamableLLMProvider {
    /**
     * Stream tokens/messages from the provider. The provider should invoke onMessage for each
     * chunk it receives and complete the stream when finished.
     */
    void stream(String agentId, String model, String prompt, Map<String, String> config, Consumer<String> onMessage) throws Exception;
}
