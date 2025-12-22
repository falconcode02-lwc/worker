package io.falconFlow.DSL.Helpers;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Global {

  private static final Map<String, String> trackedServices = new ConcurrentHashMap<>();

  public static void addService(String beanName) {
    trackedServices.put(beanName.toLowerCase(Locale.ROOT), beanName);
  }

  public static boolean containsService(String beanName) {
    return trackedServices.containsKey(beanName.toLowerCase(Locale.ROOT));
  }

  public static String getBeanName(String beanName) {
    return trackedServices.get(beanName.toLowerCase(Locale.ROOT));
  }

  public static Map<String, String> getAll() {
    return trackedServices;
  }
}
