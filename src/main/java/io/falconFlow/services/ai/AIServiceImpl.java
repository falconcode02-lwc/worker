package io.falconFlow.services.ai;

import io.falconFlow.model.ai.AgentResponse;
import io.falconFlow.model.ai.AgentSummary;
import io.falconFlow.services.ai.memory.InMemoryMemory;
import io.falconFlow.services.ai.memory.Memory;
// n8n/tool integration removed; providers handle external calls

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.falconFlow.entity.AgentEntity;
import io.falconFlow.repository.AgentRepository;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AIServiceImpl implements AIService {
    private final Map<String, String> agentModel = new ConcurrentHashMap<>();
    private final Map<String, Memory> agentMemory = new ConcurrentHashMap<>();
    // private Map for tools removed
    private final Map<String, Map<String, String>> agentConfigs = new ConcurrentHashMap<>();
    private final AgentRepository agentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestTemplate restTemplate;
    private final Map<String, io.falconFlow.services.ai.provider.LLMProvider> providers;
    private final String defaultProviderName;

    @Autowired
    public AIServiceImpl(RestTemplate restTemplate,
                         @Autowired(required = false) Map<String, io.falconFlow.services.ai.provider.LLMProvider> providers,
                         @Autowired(required = false) AgentRepository agentRepository) {
        this.restTemplate = restTemplate;
        this.providers = providers != null ? providers : Collections.emptyMap();
        this.agentRepository = agentRepository;

        // choose a sensible default provider name if available
        if (this.providers.containsKey("chatgpt")) {
            this.defaultProviderName = "chatgpt";
        } else if (this.providers.containsKey("openai")) {
            this.defaultProviderName = "openai";
        } else if (!this.providers.isEmpty()) {
            this.defaultProviderName = this.providers.keySet().iterator().next();
        } else {
            this.defaultProviderName = null;
        }
    }

    @Override
    public void createAgent(String agentId, String model, Map<String, String> config) {
        agentModel.put(agentId, model == null ? "default" : model);
        agentMemory.put(agentId, new InMemoryMemory());
        if (config != null) {
            agentConfigs.put(agentId, config);
        }
        // persist agent to DB (if repository available)
        if (agentRepository != null) {
            try {
                AgentEntity e = new AgentEntity();
                e.setAgentId(agentId);
                e.setModel(model);
                if (config != null) e.setConfig(objectMapper.writeValueAsString(config));
                agentRepository.save(e);
            } catch (Exception ex) {
                // log and continue — not failing agent creation due to persistence issue
                System.err.println("Failed to persist agent: " + ex.getMessage());
            }
        }
    }

    @Override
    public Map<String, String> getAgentConfig(String agentId) {
        Map<String, String> cfg = agentConfigs.get(agentId);
        if (cfg != null) return cfg;
        // try load from DB
        if (agentRepository != null) {
            Optional<AgentEntity> opt = agentRepository.findById(agentId);
            if (opt.isPresent()) {
                AgentEntity e = opt.get();
                String json = e.getConfig();
                if (json != null && !json.isEmpty()) {
                    try {
                        Map<String, String> map = objectMapper.readValue(json, Map.class);
                        agentConfigs.put(agentId, map);
                        return map;
                    } catch (Exception ex) {
                        // ignore parse error
                    }
                }
            }
        }
        return null;
    }

    @Override
    public AgentResponse sendMessage(String agentId, String message) {
        String model = null;
        // Get model from DB or cache
        if (agentRepository != null) {
            Optional<AgentEntity> opt = agentRepository.findById(agentId);
            if (opt.isPresent()) {
                model = opt.get().getModel();
                if (model != null) {
                    agentModel.put(agentId, model); // update cache
                }
            }
        }
        if (model == null) {
            model = agentModel.get(agentId); // try cache
        }

        Memory mem = agentMemory.get(agentId);
        if (mem == null) {
            mem = new InMemoryMemory();
            agentMemory.put(agentId, mem);
        }

        mem.append("user: " + message);

        try {
            String responseText;
            Map<String, String> cfg = getAgentConfig(agentId);
            // select provider by config.provider or use default
            io.falconFlow.services.ai.provider.LLMProvider selected = null;
            String providerName = cfg != null ? cfg.get("provider") : null;
            if (providerName != null && providers.containsKey(providerName)) {
                selected = providers.get(providerName);
            } else if (providerName != null) {
                // try lowercase key
                String lower = providerName.toLowerCase();
                if (providers.containsKey(lower)) selected = providers.get(lower);
            }
            if (selected == null && defaultProviderName != null) {
                selected = providers.get(defaultProviderName);
            }

            if (selected != null) {
                responseText = selected.generate(agentId, model, message, cfg);
            } else {
                // fallback to simulated response
                responseText = String.format("[simulated response from %s] Echo: %s", model, message);
            }

            mem.append("assistant: " + responseText);
            return new AgentResponse(agentId, responseText);
        } catch (Exception e) {
            throw new RuntimeException("LLM provider call failed", e);
        }
    }

    @Override
    public Object getMemory(String agentId) {
        Memory mem = agentMemory.get(agentId);
        if (mem == null) return null;
        return mem.history();
    }

    @Override
    public List<AgentSummary> listAgents() {
        if (agentRepository == null) {
            // No DB - return empty list (or could return cached agents)
            return Collections.emptyList();
        }

        List<AgentSummary> agents = new ArrayList<>();
        for (AgentEntity agent : agentRepository.findAll()) {
            String provider = null;
            try {
                Map<String,String> config = agent.getConfig() != null ? 
                    objectMapper.readValue(agent.getConfig(), Map.class) : null;
                if (config != null) {
                    provider = config.get("provider");
                }
            } catch (Exception ignored) {}

            agents.add(new AgentSummary(
                agent.getAgentId(),
                agent.getModel(),
                provider
            ));
        }
        return agents;
    }
}
