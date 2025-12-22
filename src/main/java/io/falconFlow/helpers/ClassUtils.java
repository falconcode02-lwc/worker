package io.falconFlow.helpers;

import java.io.IOException;
import java.io.InputStream;

public class ClassUtils {

  public static byte[] getClassBytes(Class<?> clazz) throws IOException {
    String resourceName = clazz.getName().replace('.', '/') + ".class";
    try (InputStream is = clazz.getClassLoader().getResourceAsStream(resourceName)) {
      if (is == null) {
        throw new IOException("Class file not found for: " + clazz.getName());
      }
      return is.readAllBytes();
    }
  }
}
