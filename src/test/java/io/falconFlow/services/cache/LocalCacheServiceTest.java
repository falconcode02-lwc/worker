package io.falconFlow.services.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LocalCacheServiceTest {

    @Test
    void testCacheOps() {
        LocalCacheService service = new LocalCacheService();
        service.setCache("key1", "value1");
        Assertions.assertEquals("value1", service.getCache("key1"));

        service.setCache("key2", "value2", 100);
        Assertions.assertEquals("value2", service.getCache("key2"));
    }

    @Test
    void testExpiry() throws InterruptedException {
        LocalCacheService service = new LocalCacheService();
        // Set with 1 second TTL
        service.setCache("fastKey", "fastVal", 1);
        Assertions.assertEquals("fastVal", service.getCache("fastKey"));

        // Wait for 1.5 seconds
        Thread.sleep(1500);

        Assertions.assertNull(service.getCache("fastKey"), "Key should have expired");
    }
}
