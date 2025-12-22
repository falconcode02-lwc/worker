package io.falconFlow.interfaces;

import io.falconFlow.DSL.model.*;
import io.falconFlow.DSL.workflow.model.StateModel;
import io.falconFlow.services.isolateservices.SecretManagerService;
import io.falconFlow.services.isolateservices.StateManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FBaseClass {
    @Autowired
    SecretManagerService vault;
    public State state;
    public FRequest req;


    public final String name =  this.getClass().getSimpleName();

    public Vault getVault(InputMap mp){
        return new Vault(vault.get(mp.getStr("secret"), name));
    }

    public Vault getVault(InputMap mp, String vaultName){
        return new Vault(vault.get(mp.getStr(vaultName), name));
    }



    public State initState(StateModel _state){
        this.state = new StateManagerService(_state);
        return this.state;
    }

    public StateModel getAllState(){
        return state.get();
    }

    public Map<String, Object> ctx(){
        HashMap<String, Object> map =new HashMap<>();
        map.put("state", state.getStateDecrypted().getStateValue());
        // map.put("input", );
        // map.put("userInput", userInput);
        return map;
    }



    public State getState(){
        return state;
    }
}
