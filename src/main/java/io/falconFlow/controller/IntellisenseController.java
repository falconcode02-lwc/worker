package io.falconFlow.controller;

import io.falconFlow.DSL.model.FRequest;
import io.falconFlow.DSL.model.FunctionResponse;
import io.falconFlow.dto.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/intellisense")
public class IntellisenseController {

    @GetMapping("/classes")
    public IntellisenseMetadata getIntellisenseMetadata() {
        List<ClassMetadata> classes = new ArrayList<>();
        List<AnnotationMetadata> annotations = new ArrayList<>();

        // Add FalconFlow classes
        classes.add(extractClassMetadata(FRequest.class));
        classes.add(extractClassMetadata(FunctionResponse.class));
        classes.add(extractMapMetadata());
        classes.add(extractHashMapMetadata());

        // Add FalconFlow annotations
        annotations.add(new AnnotationMetadata(
                "@FPlugin",
                "@FPlugin",
                "Marks a class as a FalconFlow plugin"
        ));
        annotations.add(new AnnotationMetadata(
                "@FResource",
                "@FResource(name=\"${1:Resource Name}\", description=\"${2:Description}\")",
                "Defines a resource/function in a plugin"
        ));
        annotations.add(new AnnotationMetadata(
                "@FParam",
                "@FParam(value=\"${1:paramName}\", description=\"${2:Parameter description}\", required=false)",
                "Defines a parameter for a resource method"
        ));
        annotations.add(new AnnotationMetadata(
                "@useplugin",
                "@useplugin",
                "Directive to use plugin functionality"
        ));
        annotations.add(new AnnotationMetadata(
                "@useapi",
                "@useapi",
                "Directive to use API calling functionality"
        ));
        annotations.add(new AnnotationMetadata(
                "@run",
                "@run",
                "Directive to run a task or method"
        ));
        annotations.add(new AnnotationMetadata(
                "@onload",
                "@onload\\nvoid init() {\\n\\t$0\\n}",
                "Initialisation method for the plugin"
        ));
        annotations.add(new AnnotationMetadata(
                "@println",
                "@println(${1:value});",
                "Print a value to the log"
        ));
        annotations.add(new AnnotationMetadata(
                "@new",
                "@new ",
                "Instantiate a new FalconFlow DTO or Entity"
        ));

        return new IntellisenseMetadata(classes, annotations);
    }

    private ClassMetadata extractClassMetadata(Class<?> clazz) {
        List<MethodMetadata> methods = new ArrayList<>();
        List<FieldMetadata> fields = new ArrayList<>();

        // Extract public methods
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                List<ParameterMetadata> params = new ArrayList<>();
                for (Parameter param : method.getParameters()) {
                    params.add(new ParameterMetadata(
                            param.getName(),
                            param.getType().getSimpleName()
                    ));
                }

                String documentation = generateMethodDocumentation(method.getName());
                methods.add(new MethodMetadata(
                        method.getName(),
                        method.getReturnType().getSimpleName(),
                        params,
                        documentation
                ));
            }
        }

        // Extract public fields
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers())) {
                fields.add(new FieldMetadata(
                        field.getName(),
                        field.getType().getSimpleName()
                ));
            }
        }

        String type = clazz.isInterface() ? "interface" : clazz.isEnum() ? "enum" : "class";
        return new ClassMetadata(clazz.getSimpleName(), type, methods, fields);
    }

    private ClassMetadata extractMapMetadata() {
        List<MethodMetadata> methods = new ArrayList<>();

        methods.add(new MethodMetadata(
                "put",
                "V",
                Arrays.asList(
                        new ParameterMetadata("key", "K"),
                        new ParameterMetadata("value", "V")
                ),
                "Adds a key-value pair to the map"
        ));
        methods.add(new MethodMetadata(
                "get",
                "V",
                Arrays.asList(new ParameterMetadata("key", "K")),
                "Gets a value from the map"
        ));
        methods.add(new MethodMetadata(
                "size",
                "int",
                new ArrayList<>(),
                "Returns the size of the map"
        ));
        methods.add(new MethodMetadata(
                "isEmpty",
                "boolean",
                new ArrayList<>(),
                "Checks if the map is empty"
        ));
        methods.add(new MethodMetadata(
                "containsKey",
                "boolean",
                Arrays.asList(new ParameterMetadata("key", "K")),
                "Checks if the map contains a key"
        ));

        return new ClassMetadata("Map", "interface", methods, new ArrayList<>());
    }

    private ClassMetadata extractHashMapMetadata() {
        // HashMap has the same methods as Map for our purposes
        return new ClassMetadata("HashMap", "class", extractMapMetadata().getMethods(), new ArrayList<>());
    }

    private String generateMethodDocumentation(String methodName) {
        // Generate basic documentation from method name
        if (methodName.startsWith("get")) {
            String property = methodName.substring(3);
            return "Returns the " + camelCaseToWords(property);
        } else if (methodName.startsWith("set")) {
            String property = methodName.substring(3);
            return "Sets the " + camelCaseToWords(property);
        } else if (methodName.startsWith("is")) {
            String property = methodName.substring(2);
            return "Checks if " + camelCaseToWords(property);
        }
        return methodName;
    }

    private String camelCaseToWords(String camelCase) {
        return camelCase.replaceAll("([A-Z])", " $1").toLowerCase().trim();
    }
}
