package io.falconFlow.configuration.MCP;

import io.falconFlow.DSL.annotation.FPlugin;
import io.falconFlow.interfaces.FParam;
import io.falconFlow.interfaces.FResource;
import io.falconFlow.interfaces.MCPToolRegistry;
import io.falconFlow.model.MCPToolDefinition;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
//@Component
public class MCPToolScanner implements ApplicationListener<ApplicationReadyEvent> {

    private final ApplicationContext context;
    private final MCPToolRegistry registry;

    public MCPToolScanner(
            ApplicationContext context,
            MCPToolRegistry registry
    ) {
        this.context = context;
        this.registry = registry;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        scanAndRegisterTools();
    }

    private void scanAndRegisterTools() {

        for (String beanName : context.getBeanDefinitionNames()) {

            Object bean = context.getBean(beanName);
            Class<?> clazz = AopUtils.getTargetClass(bean);
            if(clazz.getAnnotation(FPlugin.class) == null){
                continue;
            }

            for (Method method : clazz.getDeclaredMethods()) {

                FResource tool = method.getAnnotation(FResource.class);
                if (tool == null) continue;

                MCPToolDefinition def = new MCPToolDefinition();
                def.setName(method.getName());
                def.setDescription(tool.description());
                def.setMethod(method);
                def.setBean(bean);
                def.setInputSchema(resolveSchema(method));

                registry.register(def);
            }
        }
    }

    private Map<String, Map<String, String>> resolveSchema(Method method) {
        Map<String, Map<String, String>> schema = new LinkedHashMap<>();
        for (Parameter p : method.getParameters()) {
            FParam ann = p.getAnnotation(FParam.class);
            Map<String, String> details = new HashMap<>();
            if (ann != null) {
                details.put("type", p.getType().getSimpleName());
                details.put("description", ann.description());
                details.put("required", String.valueOf(ann.required()));
                schema.put(ann.value(), details);
            } else {
                details.put("type", p.getType().getSimpleName());
                details.put("required", "false");
                schema.put(p.getName(), details);
            }
        }
        return schema;
    }

}


