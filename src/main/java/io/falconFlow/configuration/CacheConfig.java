package io.falconFlow.configuration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  // Cache names as constants
  public static final String FORMS_CACHE = "forms";
  public static final String WORKFLOWS_CACHE = "workflows";
  public static final String FUNCTIONS_CACHE = "functions";
  public static final String PLUGINS_CACHE = "plugins";
  public static final String SECRETS_CACHE = "secrets";
  public static final String CLASS_TYPES_CACHE = "classTypes";
  public static final String STATE_DECRYPTED_CACHE = "state_decrypted";
    public static final String VaultCache = "VaultCache";
}
