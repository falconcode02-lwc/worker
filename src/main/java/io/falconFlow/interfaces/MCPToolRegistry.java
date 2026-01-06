package io.falconFlow.interfaces;

import io.falconFlow.model.MCPToolDefinition;

import java.util.Collection;

public interface MCPToolRegistry {
    void register(MCPToolDefinition tool);
    MCPToolDefinition get(String name);
    Collection<MCPToolDefinition> list();
}
