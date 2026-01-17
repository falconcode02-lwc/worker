package io.falconFlow.dto;

import java.util.List;

public class MethodMetadata {
    private String name;
    private String returnType;
    private List<ParameterMetadata> parameters;
    private String documentation;

    public MethodMetadata() {
    }

    public MethodMetadata(String name, String returnType, List<ParameterMetadata> parameters, String documentation) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.documentation = documentation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<ParameterMetadata> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterMetadata> parameters) {
        this.parameters = parameters;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }
}
