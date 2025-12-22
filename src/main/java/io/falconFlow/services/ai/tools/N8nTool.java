package io.falconFlow.services.ai.tools;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

public class N8nTool implements Tool {
    private final String name = "n8n";
    private final RestTemplate restTemplate;
    private final String webhookUrl;

    public N8nTool(RestTemplate restTemplate, String webhookUrl) {
        this.restTemplate = restTemplate;
        this.webhookUrl = webhookUrl;
    }

    @Override
    public String getName() { return name; }

    @Override
    public Object execute(Map<String, Object> input) throws Exception {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            throw new IllegalArgumentException("n8n webhookUrl is not configured");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(input, headers);

        ResponseEntity<String> resp = restTemplate.postForEntity(webhookUrl, entity, String.class);
        return resp.getBody();
    }
}
