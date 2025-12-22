package io.falconFlow.services.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.falconFlow.services.secret.SecretManager;
import io.falconFlow.services.ai.provider.ProviderConfigService;
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

@Component("openai")
public class OpenAIProvider implements LLMProvider {

    private final RestTemplate restTemplate;
    private final SecretManager secretManager;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ProviderConfigService providerConfigService;

    @Autowired
    public OpenAIProvider(RestTemplate restTemplate, SecretManager secretManager, ProviderConfigService providerConfigService) {
        this.restTemplate = restTemplate;
        this.secretManager = secretManager;
        this.providerConfigService = providerConfigService;
    }

    @Override
    public String generate(String agentId, String model, String prompt, Map<String, String> config) throws Exception {
        if (model != null && model.equals("default")) {
            // never send "default" to OpenAI API
            model = null;
        }

        // Determine API key: prefer secret referenced by name in config, else direct config value
        String apiKey = null;
        if (config != null) {
            String secretName = config.getOrDefault("ai.keySecretName", config.get("openai.secretName"));
            if (secretName != null && !secretName.isEmpty()) {
                apiKey = secretManager.getSecretValue(secretName).orElse(null);
            }
            if (apiKey == null) {
                apiKey = config.get("openai.apiKey");
            }
        }

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API key not configured for agent=" + agentId);
        }

    // load api url from database-configured provider config (fallback to default URL)
    String url = providerConfigService.getApiUrl("openai", "https://api.openai.com/v1/chat/completions");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String,Object> body = new HashMap<>();
        // determine model: prefer explicit call model, else provider-config default, else hardcoded default
        String useModel = model;
        if (useModel == null || useModel.isEmpty()) {
            useModel = providerConfigService.getDefaultModel("openai", "gpt-3.5-turbo");
        }
        body.put("model", useModel);

        // messages array
        Map<String,String> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", prompt == null ? "" : prompt);
        body.put("messages", List.of(msg));
        body.put("max_tokens", 800);

        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(body), headers);

        ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("OpenAI call failed: " + resp.getStatusCodeValue() + " - " + resp.getBody());
        }

        JsonNode root = mapper.readTree(resp.getBody());
        // parse choices[0].message.content
        JsonNode choices = root.get("choices");
        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode first = choices.get(0);
            JsonNode message = first.get("message");
            if (message != null && message.get("content") != null) {
                return message.get("content").asText();
            }
            if (first.get("text") != null) {
                return first.get("text").asText();
            }
        }

        // fallback to raw 'text' or full body
        if (root.get("text") != null) return root.get("text").asText();
        return resp.getBody();
    }
}
