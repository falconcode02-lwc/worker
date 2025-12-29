package io.falconFlow.DSL.model;



import java.util.HashMap;
import java.util.Map;

public class InputMap extends HashMap<String, Object> {


    public InputMap() {}

    public InputMap(Map<String, Object> source) {
        super(source);        // Copies all entries into InputMap
    }

    public String getStr(String key) {
        Object val = this.get(key);
        return val == null ? null : val.toString();
    }

    public String getStr(String key, String defaultValue) {
        Object val = this.get(key);
        return val == null ? defaultValue : val.toString();
    }

    public int getInt(String key, int defaultValue) {
        Object val = this.get(key);
        if (val == null) {
            return defaultValue;
        }
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBool(String key) {
        return getBool(key, false);
    }

    public boolean getBool(String key, boolean defaultValue) {
        Object val = this.get(key);
        if (val == null) {
            return defaultValue;
        }
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        return Boolean.parseBoolean(val.toString());
    }


}
