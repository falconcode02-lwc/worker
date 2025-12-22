package io.falconFlow.core;

import java.util.HashMap;
import java.util.Map;

public class DynamicApiRegistry {
    private static final Map<String, Map<String, String>> data = new HashMap<>();

    public static void register(String name, Map<String, String> meta) {
        data.put(name, meta);
    }

    public static Map<String, Map<String, String>> getAll() {
        return data;
    }

    public static void unregister(String name) {
        data.remove(name.toLowerCase());
    }
}
