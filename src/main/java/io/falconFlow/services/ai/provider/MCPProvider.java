package io.falconFlow.services.ai.provider;

import io.falconFlow.services.ai.mcp.MCPClient;
import io.falconFlow.services.secret.SecretManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import io.falconFlow.services.ai.provider.StreamableLLMProvider;

@Component("mcp")
public class MCPProvider implements LLMProvider, StreamableLLMProvider {

    private final ProviderConfigService providerConfigService;
    private final SecretManager secretManager;
    private final MCPClient mcpClient;

    @Autowired
    public MCPProvider(ProviderConfigService providerConfigService, SecretManager secretManager, MCPClient mcpClient) {
        this.providerConfigService = providerConfigService;
        this.secretManager = secretManager;
        this.mcpClient = mcpClient;
    }

    @Override
    public String generate(String agentId, String model, String prompt, Map<String, String> config) throws Exception {
        // load endpoint from DB provider config
        String url = providerConfigService.getApiUrl("mcp", null);
        if (url == null) {
            throw new IllegalStateException("MCP provider not configured (no apiUrl)");
        }

        String useModel = model;
        if (useModel == null || useModel.isEmpty()) {
            useModel = providerConfigService.getDefaultModel("mcp", null);
        }

        // Resolve API key/secret
        String apiKey = null;
        if (config != null) {
            String secretName = config.getOrDefault("mcp.secretName", config.get("ai.keySecretName"));
            if (secretName != null && !secretName.isEmpty()) {
                apiKey = secretManager.getSecretValue(secretName).orElse(null);
            }
            if (apiKey == null) apiKey = config.get("mcp.apiKey");

            // Provider-level secret: check provider config properties for a tokenSecretName
            if (apiKey == null) {
                String tokenSecret = providerConfigService.getProperty("mcp", "tokenSecretName").orElse(null);
                if (tokenSecret != null && !tokenSecret.isEmpty()) {
                    apiKey = secretManager.getSecretValue(tokenSecret).orElse(null);
                }
            }
        }

        Map<String,Object> meta = new HashMap<>();
        if (apiKey != null) meta.put("authToken", apiKey);

        // pass through any provider-specific parameters from config
        if (config != null && config.containsKey("mcp.parameters")) {
            // expect JSON string in config.mcp.parameters
            meta.put("parameters", config.get("mcp.parameters"));
        }

        return mcpClient.generate(url, useModel, prompt, meta);
    }

    @Override
    public void stream(String agentId, String model, String prompt, Map<String, String> config, Consumer<String> onMessage) throws Exception {
        String url = providerConfigService.getApiUrl("mcp", null);
        if (url == null) throw new IllegalStateException("MCP provider not configured (no apiUrl)");

        String useModel = model;
        if (useModel == null || useModel.isEmpty()) {
            useModel = providerConfigService.getDefaultModel("mcp", null);
        }

        // Resolve API key/secret (same logic as generate)
        String apiKey = null;
        if (config != null) {
            String secretName = config.getOrDefault("mcp.secretName", config.get("ai.keySecretName"));
            if (secretName != null && !secretName.isEmpty()) {
                apiKey = secretManager.getSecretValue(secretName).orElse(null);
            }
            if (apiKey == null) apiKey = config.get("mcp.apiKey");
        }
        if (apiKey == null) {
            String tokenSecret = providerConfigService.getProperty("mcp", "tokenSecretName").orElse(null);
            if (tokenSecret != null && !tokenSecret.isEmpty()) {
                apiKey = secretManager.getSecretValue(tokenSecret).orElse(null);
            }
        }

        Map<String,Object> meta = new HashMap<>();
        if (apiKey != null) meta.put("authToken", apiKey);
        if (config != null && config.containsKey("mcp.parameters")) {
            meta.put("parameters", config.get("mcp.parameters"));
        }

        mcpClient.stream(url, useModel, prompt, meta, onMessage);
    }
}
