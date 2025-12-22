package io.falconFlow.services.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.falconFlow.services.secret.SecretManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal Gemini provider adapter. Reads apiUrl/defaultModel from ProviderConfigEntity (via ProviderConfigService)
 * and API key via agent config (gemini.secretName / ai.keySecretName / gemini.apiKey) or provider-level tokenSecretName.
 *
 * The exact Gemini REST contract may vary depending on your MCP/adapter; configure provider config accordingly.
 */
@Component("gemini")
public class GeminiProvider implements LLMProvider {

    private final RestTemplate restTemplate;
    private final SecretManager secretManager;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ProviderConfigService providerConfigService;

    @Autowired
    public GeminiProvider(RestTemplate restTemplate, SecretManager secretManager, ProviderConfigService providerConfigService) {
        this.restTemplate = restTemplate;
        this.secretManager = secretManager;
        this.providerConfigService = providerConfigService;
    }

    @Override
    public String generate(String agentId, String model, String prompt, Map<String, String> config) throws Exception {
        // Resolve api key
        String apiKey = null;
        if (config != null) {
            String secretName = config.getOrDefault("ai.keySecretName", config.get("gemini.secretName"));
//            if (secretName != null && !secretName.isEmpty()) {
//                apiKey = secretManager.getSecretValue(secretName).orElse(null);
//            }
            if (apiKey == null) apiKey = config.get("gemini.apiKey");
        }

        if (apiKey == null || apiKey.isEmpty()) {
            // provider-level tokenSecretName
            String tokenSecret = providerConfigService.getProperty("gemini", "tokenSecretName").orElse(null);
            if (tokenSecret != null && !tokenSecret.isEmpty()) {
                apiKey = secretManager.getSecretValue(tokenSecret).orElse(null);
            }
        }

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key not configured for agent=" + agentId);
        }

        // api url must be configured in ProviderConfigEntity. No hardcoded default here.
        String url = providerConfigService.getApiUrl("gemini", null);
        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("Gemini provider not configured (apiUrl)");
        }

        String useModel = model;
        if (useModel == null || useModel.isEmpty()) {
            useModel = providerConfigService.getDefaultModel("gemini", null);
        }
        if (useModel == null || useModel.isEmpty()) {
            throw new IllegalStateException("No model specified for Gemini provider (agent=" + agentId + ")");
        }


        url = url + "/v1beta/models/" + useModel + ":generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt == null ? "" : prompt)
                        ))
                )
        );



        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(body), headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Gemini call failed: " + resp.getStatusCodeValue() + " - " + resp.getBody());
        }

        String bodyStr = resp.getBody();
        try {
            JsonNode root = mapper.readTree(resp.getBody());
            JsonNode textNode = root.at("/candidates/0/content/parts/0/text");
            return textNode.isMissingNode() ? resp.getBody() : textNode.asText();
        } catch (Exception ignored) {
            // fall back to raw body
        }

        return bodyStr == null ? "" : bodyStr;
    }
}
