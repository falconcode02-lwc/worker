package io.falconFlow.model;

import java.lang.reflect.Method;
import java.util.Map;

public class MCPToolDefinition {
    private String name;
    private String description;
    private Map<String, String> inputSchema;
    private Method method;
    private Object bean;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getInputSchema() {
        return inputSchema;
    }

    public void setInputSchema(Map<String, String> inputSchema) {
        this.inputSchema = inputSchema;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }
}
