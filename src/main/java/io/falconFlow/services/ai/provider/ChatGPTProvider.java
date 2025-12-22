package io.falconFlow.services.ai.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ChatGPTProvider is a thin adapter that delegates to OpenAIProvider with ChatGPT-style defaults.
 */
@Component("chatgpt")
public class ChatGPTProvider implements LLMProvider {

    private final OpenAIProvider openAIProvider;

    @Autowired
    public ChatGPTProvider(OpenAIProvider openAIProvider) {
        this.openAIProvider = openAIProvider;
    }

    @Override
    public String generate(String agentId, String model, String prompt, Map<String, String> config) throws Exception {
        // prefer explicit model, otherwise use a ChatGPT default
        String useModel = model != null ? model : (config != null ? config.getOrDefault("model", "gpt-3.5-turbo") : "gpt-3.5-turbo");
        return openAIProvider.generate(agentId, useModel, prompt, config);
    }
}
