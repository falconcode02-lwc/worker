package io.falconFlow.configuration.MCP;

import io.falconFlow.interfaces.MCPToolRegistry;
import io.falconFlow.model.MCPToolDefinition;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

@Component
public class MCPToolExecutor {

    private final MCPToolRegistry registry;

    public MCPToolExecutor(MCPToolRegistry registry) {
        this.registry = registry;
    }

    public Object execute(String toolName, Map<String, Object> args) {

        MCPToolDefinition tool = registry.get(toolName);
        if (tool == null) {
            throw new RuntimeException("Tool not found: " + toolName);
        }

        Method method = tool.getMethod();
        Object[] params = Arrays.stream(method.getParameters())
                .map(p -> args.get(p.getName()))
                .toArray();

        try {
            return method.invoke(tool.getBean(), params);
        } catch (Exception e) {
            throw new RuntimeException("Tool execution failed", e);
        }
    }
}

