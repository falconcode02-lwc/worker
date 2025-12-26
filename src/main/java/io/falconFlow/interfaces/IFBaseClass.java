package io.falconFlow.interfaces;

import io.falconFlow.DSL.model.InputMap;
import io.falconFlow.DSL.model.State;
import io.falconFlow.DSL.model.Vault;
import io.falconFlow.DSL.workflow.model.StateModel;

import java.util.Map;

public interface IFBaseClass {

    Vault getVault(InputMap mp);
    Vault getVault(InputMap mp, String vaultName);
    void setCache(String key, Object value);
    void setCache(String key, Object value, int ttl);
    Object getCache(String key);
    State initState(StateModel _state);
    StateModel getAllState();
    Map<String, Object> ctx();
    State getState();



}
