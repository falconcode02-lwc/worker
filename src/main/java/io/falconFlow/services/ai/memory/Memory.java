package io.falconFlow.services.ai.memory;

import java.util.List;

public interface Memory {
    void append(String entry);
    List<String> history();
    void clear();
}
