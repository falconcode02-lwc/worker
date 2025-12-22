package io.falconFlow.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicClassLoader extends ClassLoader {
  private final Map<String, byte[]> definedClasses = new ConcurrentHashMap<>();

  public DynamicClassLoader(ClassLoader parent) {
    super(parent);
  }

  public Class<?> defineClass(String name, byte[] bytes) {
    definedClasses.put(name, bytes);
    return super.defineClass(name, bytes, 0, bytes.length);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    byte[] bytes = definedClasses.get(name);
    if (bytes != null) {
      return defineClass(name, bytes, 0, bytes.length);
    }
    return super.findClass(name);
  }

  /** Load multiple compiled classes at once */
  public void defineAll(Map<String, byte[]> classes) {
    classes.forEach(this::defineClass);
  }
}
