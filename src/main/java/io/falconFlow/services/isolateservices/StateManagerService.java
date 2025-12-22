package io.falconFlow.services.isolateservices;

import io.falconFlow.configuration.CacheConfig;
import io.falconFlow.configuration.SpringContextHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.falconFlow.DSL.model.State;
import io.falconFlow.DSL.utils.AESUtil;
import io.falconFlow.DSL.workflow.model.StateModel;
import org.springframework.cache.annotation.Cacheable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StateManagerService implements State {

  private final StateModel state;
  private static final ObjectMapper mapper = new ObjectMapper();

public StateManagerService(StateModel state) {
    if (state == null) {
      this.state = new StateModel();
    } else {
      this.state = state;
      if (this.state.getStateValue() == null) {
        this.state.setStateValue(new ConcurrentHashMap<>());
      }
    }
  }

    @Override
    public StateModel get() {
    return state;
  }


  public StateModel getStateDecrypted() {
    try {
      StateModel stm = new StateModel();
      Map<String, Object> d = new HashMap<>();
      StateCacheService cacheService = SpringContextHelper.getBean(StateCacheService.class);

      for (Map.Entry<String, Object> entry : state.getStateValue().entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        if (cacheService != null) {
          d.put(key, cacheService.decryptAndParse(key, value.toString()));
        } else {
          d.put(key, mapper.readValue(AESUtil.decrypt(value.toString()), Object.class));
        }
      }
      stm.setStateValue(d);
      return stm;
    } catch (Exception e) {
      throw new RuntimeException("Error decrypting state value", e);
    }
  }

  // 🔹 Encrypt and store value as Base64
  @Override
  public void set(String key, Object value) {
    if (key == null) return;
    try {
      String json = mapper.writeValueAsString(value);
      String encrypted = AESUtil.encrypt(json);
      state.getStateValue().put(key, encrypted);
    } catch (Exception e) {
      throw new RuntimeException("Error encrypting state value for key: " + key, e);
    }
  }

  // 🔹 Decrypt value and convert back to Object


  @Override
  public Object get(String key) {
    if (key == null) return null;
    Object stored = state.getStateValue().get(key);
    if (stored == null) return null;

    StateCacheService cacheService = SpringContextHelper.getBean(StateCacheService.class);
    if (cacheService != null) {
      return cacheService.decryptAndParse(key, stored.toString());
    }

    try {
      String decrypted = AESUtil.decrypt(stored.toString());
      return mapper.readValue(decrypted, Object.class);
    } catch (Exception e) {
      throw new RuntimeException("Error decrypting state value for key: " + key, e);
    }
  }

  // 🔹 Type-safe getter\
  @Override
  public <T> T get(String key, Class<T> type) {
    if (key == null) return null;
    Object stored = state.getStateValue().get(key);
    if (stored == null) return null;

    StateCacheService cacheService = SpringContextHelper.getBean(StateCacheService.class);
    if (cacheService != null) {
      return cacheService.decryptAndParse(key, stored.toString(), type);
    }

    try {
      String decrypted = AESUtil.decrypt(stored.toString());
      return mapper.readValue(decrypted, type);
    } catch (Exception e) {
      throw new RuntimeException("Error decrypting state value for key: " + key, e);
    }
  }

  @Override
  public void remove(String key) {
    if (key != null) state.getStateValue().remove(key);
  }

  @Override
  public void clear() {
    state.getStateValue().clear();
  }

  @Override
  public boolean contains(String key) {
    return state.getStateValue().containsKey(key);
  }

  @Override
  public String toString() {
    return "StateManagerService{" + "state=" + state + '}';
  }
}
