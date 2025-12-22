package io.falconFlow.services.ai.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryMemory implements Memory {
    private final List<String> entries = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void append(String entry) {
        entries.add(entry);
    }

    @Override
    public List<String> history() {
        return new ArrayList<>(entries);
    }

    @Override
    public void clear() {
        entries.clear();
    }
}
