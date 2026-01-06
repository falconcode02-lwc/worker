package io.falconFlow.configuration.MCP;

import io.falconFlow.interfaces.MCPToolRegistry;
import io.falconFlow.model.MCPToolDefinition;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryMCPToolRegistry implements MCPToolRegistry {

    private final Map<String, MCPToolDefinition> tools = new ConcurrentHashMap<>();

    @Override
    public void register(MCPToolDefinition tool) {
        tools.put(tool.getName(), tool);
    }

    @Override
    public MCPToolDefinition get(String name) {
        return tools.get(name);
    }

    @Override
    public Collection<MCPToolDefinition> list() {
        return tools.values();
    }
}

