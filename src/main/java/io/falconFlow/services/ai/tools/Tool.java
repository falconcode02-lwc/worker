package io.falconFlow.services.ai.tools;

import java.util.Map;

public interface Tool {
    String getName();
    Object execute(Map<String, Object> input) throws Exception;
}
