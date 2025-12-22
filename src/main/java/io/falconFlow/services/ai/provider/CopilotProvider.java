package io.falconFlow.services.ai.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * CopilotProvider delegates to OpenAIProvider but defaults to code-focused models.
 * GitHub Copilot doesn't have a public HTTP API here, so we treat it as a model/default mapping.
 */
@Component("copilot")
public class CopilotProvider implements LLMProvider {

    private final OpenAIProvider openAIProvider;

    @Autowired
    public CopilotProvider(OpenAIProvider openAIProvider) {
        this.openAIProvider = openAIProvider;
    }

    @Override
    public String generate(String agentId, String model, String prompt, Map<String, String> config) throws Exception {
        // default to a code-capable model if none provided
        String useModel = model != null ? model : (config != null ? config.getOrDefault("model", "gpt-4o-mini-code") : "gpt-4o-mini-code");
        return openAIProvider.generate(agentId, useModel, prompt, config);
    }
}
