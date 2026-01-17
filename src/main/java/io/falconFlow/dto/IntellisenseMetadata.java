package io.falconFlow.dto;

import java.util.List;

public class IntellisenseMetadata {
    private List<ClassMetadata> classes;
    private List<AnnotationMetadata> annotations;

    public IntellisenseMetadata() {
    }

    public IntellisenseMetadata(List<ClassMetadata> classes, List<AnnotationMetadata> annotations) {
        this.classes = classes;
        this.annotations = annotations;
    }

    public List<ClassMetadata> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassMetadata> classes) {
        this.classes = classes;
    }

    public List<AnnotationMetadata> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationMetadata> annotations) {
        this.annotations = annotations;
    }
}
