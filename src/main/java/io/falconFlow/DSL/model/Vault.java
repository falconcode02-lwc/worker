package io.falconFlow.DSL.model;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


public class Vault extends HashMap<String, Object> {

    public Vault(Map<String, Object> source) {
        super(source); // Copies all entries into InputMap
    }


    public String getValue(String key){
        if(this.containsKey(key)){
            return this.get(key).toString();
        }
        return null;
    }

    public void setValue(String name, String value){

    }



}
