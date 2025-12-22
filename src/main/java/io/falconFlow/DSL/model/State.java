package io.falconFlow.DSL.model;

import io.falconFlow.DSL.workflow.model.StateModel;

public interface State {


    StateModel get();

    // 🔹 Encrypt and store value as Base64
    void set(String key, Object value);

    // 🔹 Decrypt value and convert back to Object
    Object get(String key);

    // 🔹 Type-safe getter\
    <T> T get(String key, Class<T> type);

    void remove(String key);

    void clear();

    boolean contains(String key);

    StateModel getStateDecrypted();
}
