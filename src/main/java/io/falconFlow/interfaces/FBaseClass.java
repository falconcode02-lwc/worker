package io.falconFlow.interfaces;

import io.falconFlow.DSL.model.*;
import io.falconFlow.DSL.workflow.model.StateModel;
import io.falconFlow.services.cache.ICacheService;
import io.falconFlow.services.isolateservices.SecretManagerService;
import io.falconFlow.services.isolateservices.StateManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class FBaseClass implements IFBaseClass   {

    @Autowired
    SecretManagerService vault;
    @Autowired
    ICacheService cacheService;

    public State state;
    public final String name =  this.getClass().getSimpleName();

    @Override
    public Vault getVault(InputMap mp){
        return new Vault(vault.get(mp.getStr("secret"), name));
    }

    @Override
    public Vault getVault(InputMap mp, String vaultName){
        return new Vault(vault.get(mp.getStr(vaultName), name));
    }

    @Override
    public State initState(StateModel _state){
        this.state = new StateManagerService(_state);
        return this.state;
    }

    @Override
    public StateModel getAllState(){
        return state.get();
    }

    @Override
    public Map<String, Object> ctx(){
        HashMap<String, Object> map =new HashMap<>();
        map.put("state", state.getStateDecrypted().getStateValue());
        // map.put("input", );
        // map.put("userInput", userInput);
        return map;
    }

    @Override
    public State getState(){

        return state;
    }




    @Override
    public void setCache(String key, Object value) {
        cacheService.setCache(key, value);
    }

    @Override
    public void setCache(String key, Object value, int ttl) {
        cacheService.setCache(key, value, ttl);
    }

    @Override
    public Object getCache(String key) {
        return cacheService.getCache(key);
    }
}
