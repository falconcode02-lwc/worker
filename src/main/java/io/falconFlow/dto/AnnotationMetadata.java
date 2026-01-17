package io.falconFlow.dto;

public class AnnotationMetadata {
    private String name;
    private String snippet;
    private String documentation;

    public AnnotationMetadata() {
    }

    public AnnotationMetadata(String name, String snippet, String documentation) {
        this.name = name;
        this.snippet = snippet;
        this.documentation = documentation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }
}
