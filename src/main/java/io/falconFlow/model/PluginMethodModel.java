package io.falconFlow.model;

import java.util.Map;

public class PluginMethodModel {

    private String name;
    private String method;
    private Map<String,  Map<String, String>> properties;
    private String displayName;
    private String description;
    private boolean selected;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String,  Map<String, String>> getProperties() {
        return properties;
    }

    public void setProperties(Map<String,  Map<String, String>> properties) {
        this.properties = properties;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
