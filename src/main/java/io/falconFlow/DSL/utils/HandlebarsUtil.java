package io.falconFlow.DSL.utils;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import io.falconFlow.DSL.model.InputMap;

import java.util.HashMap;
import java.util.Map;

public class HandlebarsUtil {

    private static final Handlebars handlebars = new Handlebars();

    public static InputMap applyTemplateToMap(
            Map<String, Object> input,
            Map<String, Object> context
    ) throws Exception {

        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : input.entrySet()) {


            Object val = entry.getValue();

            if (val instanceof String) {
                Template tpl = handlebars.compileInline((String) val);
                String rendered = tpl.apply(context);
                result.put(entry.getKey(), rendered);
            } else {
                result.put(entry.getKey(), val); // no change
            }
        }
        return new InputMap(result);
    }


    public static String applyTemplate(
            String template,
            Map<String, Object> context
    ) throws Exception {
        Template tpl = handlebars.compileInline(template);
        return tpl.apply(context);
    }
}
