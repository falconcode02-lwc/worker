package io.falconFlow.model.ai;

import java.util.List;
import java.util.Map;

public class Parameters {

    private String type;
    private Map<String, Property> properties;
    private List<String> required;

    // getters & setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }
}

