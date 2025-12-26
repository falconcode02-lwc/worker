package io.falconFlow.services.cache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Primary
public class LocalCacheService implements ICacheService {

    private final Cache<String, CacheValueWrapper> cache;
    private static final int DEFAULT_TTL_SECONDS = 600; // 10 minutes default

    public LocalCacheService() {
        this.cache = Caffeine.newBuilder()
                .expireAfter(new Expiry<String, CacheValueWrapper>() {
                    @Override
                    public long expireAfterCreate(String key, CacheValueWrapper value, long currentTime) {
                        return TimeUnit.SECONDS.toNanos(value.ttlSeconds);
                    }

                    @Override
                    public long expireAfterUpdate(String key, CacheValueWrapper value, long currentTime, @NonNegative long currentDuration) {
                        return TimeUnit.SECONDS.toNanos(value.ttlSeconds);
                    }

                    @Override
                    public long expireAfterRead(String key, CacheValueWrapper value, long currentTime, @NonNegative long currentDuration) {
                        return currentDuration;
                    }
                })
                .build();
    }

    @Override
    public void setCache(String key, Object value) {
        setCache(key, value, DEFAULT_TTL_SECONDS);
    }

    @Override
    public void setCache(String key, Object value, int ttl) {
        if (value == null) {
            return;
        }
        cache.put(key, new CacheValueWrapper(value, ttl));
    }

    @Override
    public Object getCache(String key) {
        CacheValueWrapper wrapper = cache.getIfPresent(key);
        return wrapper != null ? wrapper.value : null;
    }

    private static class CacheValueWrapper {
        final Object value;
        final int ttlSeconds;

        CacheValueWrapper(Object value, int ttlSeconds) {
            this.value = value;
            this.ttlSeconds = ttlSeconds;
        }
    }
}
