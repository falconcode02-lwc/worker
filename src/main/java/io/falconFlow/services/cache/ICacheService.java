package io.falconFlow.services.cache;

public interface ICacheService {
    void setCache(String key, Object value);
    void setCache(String key, Object value, int ttl);
    Object getCache(String key);
}
