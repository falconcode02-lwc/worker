package io.falconFlow.dto;

import java.util.List;

public class ClassMetadata {
    private String name;
    private String type; // "class", "interface", "enum"
    private List<MethodMetadata> methods;
    private List<FieldMetadata> fields;

    public ClassMetadata() {
    }

    public ClassMetadata(String name, String type, List<MethodMetadata> methods, List<FieldMetadata> fields) {
        this.name = name;
        this.type = type;
        this.methods = methods;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<MethodMetadata> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodMetadata> methods) {
        this.methods = methods;
    }

    public List<FieldMetadata> getFields() {
        return fields;
    }

    public void setFields(List<FieldMetadata> fields) {
        this.fields = fields;
    }
}
