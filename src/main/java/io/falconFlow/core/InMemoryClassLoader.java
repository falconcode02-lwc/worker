package io.falconFlow.core;

import java.util.Map;

public class InMemoryClassLoader extends ClassLoader {
  private final Map<String, byte[]> compiledClasses;

  public InMemoryClassLoader(Map<String, byte[]> compiledClasses) {
    this.compiledClasses = compiledClasses;
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    byte[] bytes = compiledClasses.get(name);
    if (bytes == null) {
      return super.findClass(name);
    }
    return defineClass(name, bytes, 0, bytes.length);
  }
}
